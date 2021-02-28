package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.game.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.event.GameUpdateEvent;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.event.LeaderBoardGlobalEvent;
import com.tarantula.platform.leaderboard.LeaderBoardEntry;
import com.tarantula.platform.leaderboard.LeaderBoardSync;
import com.tarantula.platform.tournament.TournamentCreator;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
 * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
 * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
 * zxp = zxp +xp-delta
 * xp = xp + xp-delta
 */
public class GameServiceProvider implements ServiceProvider, LeaderBoard.Listener, TournamentServiceProvider {

    private JDKLogger logger = JDKLogger.getLogger(GameServiceProvider.class);
    private final String NAME;
    private static int ELO_K = 30;
    private static int LDB_SIZE = 10;
    private DataStore dataStore;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Room> roomIndex = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,Tournament> tournamentIndex = new ConcurrentHashMap<>();
    private Tournament.Creator creator;
    private EventService publisher;

    private String subscription;
    private String statisticsTag;
    private ClusterProvider integrationCluster;

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
    public Zone zone(Descriptor descriptor){//application id
        Zone zone = new Zone();
        zone.distributionKey(descriptor.distributionKey());
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
        this.creator = new TournamentCreator();
        logger.info("Game service provider ["+ NAME+"] started on ["+subscription+"]");
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
    @Override
    public boolean register(String type, Tournament.Schedule schedule,Tournament.Listener listener) {
        tournamentIndex.computeIfAbsent(type,(k)->{
            Tournament tournament = this.creator.tournament(type);
            tournament.registerListener(listener);
            return tournament;
        });
        logger.warn("tournament registered->"+type);
        return true;
    }

    @Override
    public Tournament tournament(String type) {
        return tournamentIndex.get(type);
    }
    @Override
    public void registerCreator(Tournament.Creator creator){
        this.creator = creator;
    }
}
