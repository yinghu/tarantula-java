package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.event.GameUpdateEvent;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.event.LeaderBoardGlobalEvent;
import com.tarantula.platform.leaderboard.LeaderBoardEntry;
import com.tarantula.platform.leaderboard.LeaderBoardSync;
import com.tarantula.platform.tournament.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
 * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
 * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
 * zxp = zxp +xp-delta
 * xp = xp + xp-delta
 */
public class GameServiceProvider implements ServiceProvider, LeaderBoard.Listener, TournamentServiceProvider,ItemServiceProvider,Tournament.Listener, ReloadListener {

    private JDKLogger logger = JDKLogger.getLogger(GameServiceProvider.class);
    private final String NAME;
    private static int ELO_K = 30;
    private static int LDB_SIZE = 10;
    private DataStore dataStore;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Room> roomIndex = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,Tournament> tournamentIndex = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Tournament.Instance> activeInstanceIndex = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,FilterableListener> rListeners = new ConcurrentHashMap<>();


    private EventService publisher;

    private String subscription;
    private String statisticsTag;
    private ClusterProvider integrationCluster;
    private ClusterProvider dataCluster;
    private RecoverService recoverService;
    private ServiceContext serviceContext;
    private DistributionTournamentService distributionTournamentService;
    private ConcurrentHashMap<String,Rating> rMap = new ConcurrentHashMap<>();

    public GameServiceProvider(String name){
        NAME = name;
    }
    public void statisticsTag(String statTag){
        this.statisticsTag = statTag;
    }
    public String statisticsTag(){
        return statisticsTag;
    }
    public Rating rating(String systemId){
        return rMap.computeIfAbsent(systemId,(k)->{
            Rating rating = new Rating();
            rating.distributionKey(systemId);
            this.dataStore.createIfAbsent(rating,true);
            rating.dataStore(this.dataStore);
            return rating;
        });
    }
    public void elo(Rating rating1,Rating rating2){
        double p1 = probability(rating2.elo,rating1.elo);
        double p2 = probability(rating1.elo,rating2.elo);
        if (rating1.rank-rating1.rank>0) {//1 win
            rating1.elo = rating1.elo + ELO_K * (1 - p1);
            rating2.elo = rating2.elo + ELO_K * (0 - p2);
        }
        else {//2 win
            rating1.elo = rating1.elo + ELO_K * (0 - p1);
            rating2.elo = rating2.elo + ELO_K * (1 - p2);
        }
    }
    public Statistics statistics(String systemId){
        StatisticsIndex deltaStatistics = new StatisticsIndex();
        deltaStatistics.distributionKey(systemId);
        deltaStatistics.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(deltaStatistics,true);
        return deltaStatistics;
    }
    public Zone zone(Descriptor descriptor,String mode){
        if(mode.equals(Zone.PVE)){
            Zone zone = new PVELobbySetup().load(serviceContext,descriptor);
            return zone;
        }
        else if(mode.equals(Zone.PVP)){
            Zone zone = new PVPZone();
            zone.distributionKey(descriptor.distributionKey());
            zone.mode = mode;
            return zone;
        }
        throw new UnsupportedOperationException(mode);
    }
    public PVPZone zone(Descriptor descriptor){//application id
        PVPZone zone = new PVPZone();
        zone.distributionKey(descriptor.distributionKey());
        zone.index(descriptor.tag());
        byte[] key = zone.key().asString().getBytes();
        String memberId = integrationCluster.recoverService().findDataNode(this.dataStore.name(),key);
        if(memberId!=null){
            byte[] data = integrationCluster.recoverService().load(memberId,this.dataStore.name(),key);
            zone.fromBinary(data);
            for(int i=1;i<descriptor.capacity()+1;i++){
                Arena a = new Arena(zone.bucket(),zone.oid(),i);
                data = integrationCluster.recoverService().load(memberId,this.dataStore.name(),a.key().asString().getBytes());
                if(data!=null){
                    a.fromBinary(data);
                    if(!a.disabled()){//skip disabled
                        zone.arenas.add(a);
                    }
                }
            }
        }
        else{//create local zone
            this.dataStore.createIfAbsent(zone,true);
            zone.dataStore(this.dataStore);
            for(int i=1;i<descriptor.capacity()+1;i++){
                Arena a = new Arena(zone.bucket(),zone.oid(),i);
                if(this.dataStore.load(a)){
                    if(!a.disabled()){//skip disabled
                        zone.arenas.add(a);
                    }
                }
            }
        }
        zone.dataStore(this.dataStore);
        zone.subscription = this.subscription;
        return zone;
    }
    public void addRoom(Room room){
        roomIndex.put(room.roomId,room);
    }
    public Room getRoom(String roomId){
        return roomIndex.get(roomId);
    }

    public LeaderBoard leaderBoard(String category){
        return _leaderBoard(category);
    }
    private LeaderBoardSync _leaderBoard(String category){
        return tMap.computeIfAbsent(category,(s)->{
            LeaderBoardSync ldb = new LeaderBoardSync(category,LDB_SIZE);
            ldb.dataStore(this.dataStore);
            ldb.masterListener(this);
            ldb.load();
            return ldb;
        });
    }
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.dataStore = serviceContext.dataStore(NAME.replace("-","_"),serviceContext.partitionNumber());//typeId_service
        this.publisher = serviceContext.eventService(Distributable.INTEGRATION_SCOPE);
        this.subscription = UUID.randomUUID().toString();
        integrationCluster = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
        integrationCluster.subscribe(NAME,(e)->{
            if(e instanceof LeaderBoardGlobalEvent){
                LeaderBoardEntry update = new LeaderBoardEntry(e.index(),e.name(),e.version(),e.owner(),e.balance(),e.timestamp());
                LeaderBoardSync ldb = this._leaderBoard(update.category());
                ldb.onView(update);
            }
            return false;
        });
        integrationCluster.subscribe(subscription,(e)->{
            if(e instanceof GameUpdateEvent){
                Room room = roomIndex.get(e.trackId());
                room.onUpdated(e.action(),e.payload());
            }
            return false;
        });
        this.recoverService = integrationCluster.recoverService();
        this.distributionTournamentService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionTournamentService.NAME);
        this.dataCluster = serviceContext.clusterProvider(Distributable.DATA_SCOPE);
        this.dataCluster.registerReloadListener(name(),this);
        logger.info("Game service provider ["+ NAME+"] started on ["+subscription+"]"+this.distributionTournamentService.name());
    }
    @Override
    public void atMidnight(){
        tMap.forEach((k,v)->{
            v.reset();
        });
    }
    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        logger.warn("shut down service->"+NAME);
        this.dataCluster.unregisterReloadListener(name());
        integrationCluster.unsubscribe(NAME);
    }
    private double probability(double rating1,double rating2) {
        return 1.0 * 1.0 / (1 + 1.0 * (Math.pow(10, 1.0 * (rating1 - rating2) / 400)));
    }
    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        publisher.publish(new LeaderBoardGlobalEvent(NAME,NAME,entry));
    }


    public void onClosed(Connection connection) {
        roomIndex.forEach((k,r)->r.connectionClosed(connection));
    }

    //tournament integration
    private void reload(Tournament.Listener listener){
        GameServiceIndex gameServiceIndex = new GameServiceIndex(name(),GameServiceIndex.TOURNAMENT);
        byte[] _key = gameServiceIndex.key().asString().getBytes();
        String _name = this.recoverService.findDataNode(dataStore.name(),_key);
        if(_name==null){
            return;
        }
        byte[] _data = this.recoverService.load(_name,dataStore.name(),_key);
        gameServiceIndex.fromBinary(_data);//=> list of tournament launched
        gameServiceIndex.keySet.forEach((tk)->{
            if(distributionTournamentService.localPartition(tk)){
                Tournament tournament = this.load(tk);
                if(tournament!=null){
                    tournamentIndex.put(tournament.distributionKey(),tournament);
                    listener.tournamentStarted(tournament);
                }
            }
        });
    }
    @Override
    public Tournament register(Tournament.Schedule schedule){
        byte[] ret = distributionTournamentService.schedule(name(),schedule);
        DefaultTournament tournament = new DefaultTournament();
        Map<String,Object> _map = JsonUtil.toMap(ret);
        tournament.distributionKey(_map.get("tournamentId").toString());
        tournament.fromMap(_map);
        return tournament;
    }
    public Tournament schedule(Tournament.Schedule schedule) {
        Tournament tournament = this.create(schedule);
        GameServiceIndex gameServiceIndex = new GameServiceIndex(name(),GameServiceIndex.TOURNAMENT);
        gameServiceIndex.keySet.add(tournament.distributionKey());
        if(!dataStore.createIfAbsent(gameServiceIndex,true)){
            gameServiceIndex.keySet.add(tournament.distributionKey());
            dataStore.update(gameServiceIndex);
        }
        tournamentIndex.put(tournament.distributionKey(),tournament);
        this.tournamentStarted(tournament);
        return tournament;
    }
    @Override
    public boolean available(String tournamentId){
        return this.distributionTournamentService.checkAvailable(name(),tournamentId);
    }
    @Override
    public Tournament.Instance join(String tournamentId, String systemId){
        String tid = this.distributionTournamentService.join(name(),tournamentId,systemId);
        byte[] ret = this.distributionTournamentService.enter(name(),tournamentId,tid,systemId);
        Tournament.Instance _e = new TournamentInstance();
        _e.distributionKey(tid);
        _e.fromBinary(ret);
        return _e;
    }
    @Override
    public Tournament.Entry score(String instanceId, String systemId, double delta){
        byte[] ret = this.distributionTournamentService.score(name(),instanceId,systemId,delta);
        Tournament.Entry _e = new TournamentEntry();
        _e.fromBinary(ret);
        return _e;
    }
    @Override
    public List<Tournament.Entry> list(String instanceId){
        Tournament.Instance _ins = instance(instanceId);
        return _ins.list();
    }
    public Tournament tournament(String tournamentId) {
        return tournamentIndex.get(tournamentId);
    }

    public Tournament.Instance enter(String tournamentId,String instanceId){
        return activeInstanceIndex.computeIfAbsent(instanceId,(k)->{
            TournamentInstance _ins = create(tournament(tournamentId),instanceId);
            _ins.gameServiceProvider(this);
            this.onStarted(_ins);
            return _ins;
        });
    }
    public Tournament.Instance instance(String instanceId){
        return activeInstanceIndex.computeIfAbsent(instanceId,(k)->{
            TournamentInstance _ins = new TournamentInstance();
            _ins.distributionKey(k);
            if(!dataStore.load(_ins)){
                return null;
            }
            _ins.gameServiceProvider(this);
            this.onStarted(_ins);
            return _ins;
        });
    }

    @Override
    public String registerListener(Tournament.Listener listener){
        reload(listener);
        String rid = UUID.randomUUID().toString();
        this.rListeners.put(rid,listener);
        return rid;
    }

    @Override
    public void tournamentScheduled(Tournament tournament) {
        rListeners.forEach((k,c)->{
            if(c instanceof Tournament.Listener){
                ((Tournament.Listener)c).tournamentScheduled(tournament);
            }
        });
    }
    @Override
    public void tournamentStarted(Tournament tournament) {
        rListeners.forEach((k,c)->{
            if(c instanceof Tournament.Listener){
                ((Tournament.Listener)c).tournamentStarted(tournament);
            }
        });
    }

    @Override
    public void tournamentClosed(Tournament tournament) {
        rListeners.forEach((k,c)->{
            if(c instanceof Tournament.Listener){
                ((Tournament.Listener)c).tournamentClosed(tournament);
            }
        });
    }

    @Override
    public void tournamentEnded(Tournament tournament) {
        rListeners.forEach((k,c)->{
            if(c instanceof Tournament.Listener){
                ((Tournament.Listener)c).tournamentEnded(tournament);
            }
        });
    }
    @Override
    public void onStarted(Tournament.Instance instance) {
        logger.warn("instance started->"+instance.id());
        //activeInstanceIndex.put(instance.id(),instance);
        rListeners.forEach((k,c)->{
            if(c instanceof Tournament.Listener){
                ((Tournament.Listener)c).onStarted(instance);
            }
        });
    }

    @Override
    public void onClosed(Tournament.Instance instance) {

    }

    @Override
    public void onEnded(Tournament.Instance instance) {

    }
    @Override
    public void onCreated(Tournament.Entry entry){
        logger.warn("entry created->"+entry.systemId()+">>>"+entry.owner());
    }
    @Override
    public void onUpdated(Tournament.Entry entry){
        logger.warn("entry updated->"+entry.score(0)+"-->"+entry.distributionKey());
        this.dataStore.update(entry);
    }


    public Tournament create(Tournament.Schedule schedule) {
        Tournament tournament = new DefaultTournament(schedule,this,this);
        this.dataStore.create(tournament);
        return tournament;
    }


    public Tournament load(String tournamentId) {
        DefaultTournament tournament = new DefaultTournament(this,this);
        tournament.distributionKey(tournamentId);
        String _node = recoverService.findDataNode(dataStore.name(),tournamentId.getBytes());
        if(_node==null){
            return null;
        }
        byte[] ret = recoverService.load(_node,dataStore.name(),tournamentId.getBytes());
        tournament.fromBinary(ret);
        IndexSet indexSet = new IndexSet(GameServiceIndex.TOURNAMENT_INSTANCE_JOIN);
        indexSet.distributionKey(tournamentId);
        dataStore.load(indexSet);
        indexSet.keySet.forEach((k)->{
            TournamentJoinIndex tournamentJoinIndex = new TournamentJoinIndex();
            tournamentJoinIndex.distributionKey(k);
            if(dataStore.load(tournamentJoinIndex)){
                tournamentJoinIndex.list().forEach((e)->{
                    tournament.addTournamentEntry(e);
                    this.onCreated(e);
                });
                tournament.addTournamentInstance(tournamentJoinIndex);
            }
        });
        return tournament;
    }

    public Tournament.Instance createOnJoin(Tournament tournament) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime close = start.plusMinutes(tournament.durationMinutesPerInstance()-1);
        LocalDateTime end = start.plusMinutes(tournament.durationMinutesPerInstance());
        TournamentJoinIndex tournamentInstance = new TournamentJoinIndex(tournament.maxEntriesPerInstance(),start,close,end);
        if(!dataStore.create(tournamentInstance)){
            return null;
        }
        IndexSet indexSet = new IndexSet(GameServiceIndex.TOURNAMENT_INSTANCE_JOIN);//tournament => list of join instances
        indexSet.distributionKey(tournament.distributionKey());
        indexSet.keySet.add(tournamentInstance.id());
        if(!dataStore.createIfAbsent(indexSet,true)){
            indexSet.keySet.add(tournamentInstance.id());
            dataStore.update(indexSet);
        }
        return tournamentInstance;
    }
    public TournamentInstance create(Tournament tournament,String instanceId) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime close = start.plusMinutes(tournament.durationMinutesPerInstance()-1);
        LocalDateTime end = start.plusMinutes(tournament.durationMinutesPerInstance());
        TournamentInstance tournamentInstance = new TournamentInstance(tournament.maxEntriesPerInstance(),start,close,end);
        tournamentInstance.distributionKey(instanceId);
        dataStore.createIfAbsent(tournamentInstance,true);
        IndexSet indexSet = new IndexSet(GameServiceIndex.TOURNAMENT_INSTANCE);
        indexSet.distributionKey(tournament.distributionKey());
        indexSet.keySet.add(tournamentInstance.id());
        if(!dataStore.createIfAbsent(indexSet,true)){
            indexSet.keySet.add(tournamentInstance.id());
            dataStore.update(indexSet);
        }
        TournamentEntryQuery e = new TournamentEntryQuery(instanceId);
        List<TournamentEntry> elist = query(TournamentPortableRegistry.OID,e,new String[]{instanceId});
        elist.forEach((te)->{
            te.listener(this);
            te.owner(instanceId);
            this.onCreated(te);
            tournamentInstance.addEntry(te);
        });
        return tournamentInstance;
    }
    public void updateInstance(Tournament.Instance instance){
        this.dataStore.update(instance);
    }

    public Tournament.Entry create(String systemId, Tournament.Instance instance) {
        TournamentEntry entry = new TournamentEntry(systemId,this);
        entry.owner(instance.distributionKey());
        dataStore.create(entry);
        return entry;
    }
    private <T extends Recoverable> List<T> query(int factoryId,RecoverableFactory<T> factory,String[] params){
        List<T> tlist = new ArrayList<>();
        CountDownLatch _lock = new CountDownLatch(1);
        String cid = this.serviceContext.deploymentServiceProvider().distributionCallback().registerQueryCallback((k,v)->{
            T t = factory.create();
            t.fromBinary(v);
            t.distributionKey(new String(k));
            if(!t.disabled()){
                tlist.add(t);
            }
        },()-> _lock.countDown());
        recoverService.queryStart(null,cid,dataStore.name(),factoryId,factory.registryId(),params);
        try{_lock.await();}catch (Exception ex){}
        this.serviceContext.deploymentServiceProvider().distributionCallback().removeQueryCallback(cid);
        return tlist;
    }

    @Override
    public Consumable register(Consumable consumable) {
        if(consumable.isPack()){
            consumable.list().forEach((item)->{
                this.dataStore.create(item);
                rListeners.forEach((k,c)->{
                    if(c instanceof Consumable.Listener){
                        if(c.validate(item)){
                            ((Consumable.Listener)c).onCreated(item);
                        }
                    }
                });
            });
        }
        this.dataStore.create(consumable);
        rListeners.forEach((k,c)->{
            if(c instanceof Consumable.Listener){
                if(c.validate(consumable)){
                    ((Consumable.Listener)c).onCreated(consumable);
                }
            }
        });
        return consumable;
    }
    @Override
    public Consumable update(Consumable update){
        return update;
    }
    public String registerListener(Consumable.Listener listener){
        String rid = UUID.randomUUID().toString();
        this.rListeners.put(rid,listener);
        return rid;
    }
    public void unregisterListener(String registerKey){
        rListeners.remove(registerKey);
    }

    @Override
    public void reload() {
        logger.warn("reloading ....");
    }
}
