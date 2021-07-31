package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.event.GameUpdateEvent;
import com.tarantula.platform.service.deployment.TypedListener;
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
public class GameServiceProvider implements ServiceProvider, LeaderBoard.Listener,ConfigurationServiceProvider {

    private JDKLogger logger = JDKLogger.getLogger(GameServiceProvider.class);
    private final String NAME;
    private static int ELO_K = 30;
    private static int LDB_SIZE = 10;
    private DataStore dataStore;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Room> roomIndex = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, TypedListener> rListeners = new ConcurrentHashMap<>();


    private EventService publisher;

    private String subscription;
    private String statisticsTag;
    private ClusterProvider integrationCluster;
    private ClusterProvider dataCluster;
    private RecoverService recoverService;
    private ServiceContext serviceContext;
    private DistributionTournamentService distributionTournamentService;
    private ConcurrentHashMap<String,Rating> rMap = new ConcurrentHashMap<>();

    private DistributedTournamentServiceProvider tournamentServiceProvider;

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
    public GameZone zone(Descriptor descriptor){//application id
        DynamicLobbySetup dynamicLobbySetup = new DynamicLobbySetup();
        return dynamicLobbySetup.load(serviceContext,descriptor);
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

        this.tournamentServiceProvider = new DistributedTournamentServiceProvider(NAME);
        this.tournamentServiceProvider.setup(serviceContext);
        this.tournamentServiceProvider.waitForData();
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
        this.tournamentServiceProvider.start();
    }

    @Override
    public void shutdown() throws Exception {
        logger.warn("shut down service->"+NAME);
        this.tournamentServiceProvider.shutdown();
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

    ///Configurable service provider
    @Override
    public void dataStore(OnDataStore onDataStore){
        onDataStore.on(dataStore);
    }
    @Override
    public <T extends Configurable> void register(T config) {
        rListeners.forEach((k,c)->{
            if(c.type==null||c.type.equals(config.configurationType())){
                c.listener.onCreated(config);
            }
        });
    }

    @Override
    public <T extends Configurable> void release(T t) {

    }

    @Override
    public void configure(String s) {

    }
    public <T extends Configuration> List<T> configurations(String type){
        return null;
    }
    @Override
    public String registerConfigurableListener(String type, Configurable.Listener listener) {
        String rid = UUID.randomUUID().toString();
        this.rListeners.put(rid,new TypedListener(type,listener));
        logger.warn("Listener registered with ->"+type);
        return rid;
    }
    @Override
    public void unregisterConfigurableListener(String registryKey){
        TypedListener t = rListeners.remove(registryKey);
        logger.warn("Listener removed with ->"+t.type);
    }


    //tournament service provider hook calls
    public TournamentServiceProvider tournamentServiceProvider(){
        return this.tournamentServiceProvider;
    }
    public Tournament schedule(Tournament.Schedule schedule) {
        return this.tournamentServiceProvider.schedule(schedule);
    }
    public Tournament tournament(String tournamentId){
        return this.tournamentServiceProvider.tournament(tournamentId);
    }

    public Tournament.Instance instance(String tournamentId,String instanceId){
        return this.tournamentServiceProvider.instance(tournamentId,instanceId);
    }
    public Tournament.Instance instance(String instanceId) {
        return this.tournamentServiceProvider.instance(instanceId);
    }

}
