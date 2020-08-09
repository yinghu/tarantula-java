package com.tarantula.game.service;

import com.tarantula.*;
import com.tarantula.game.*;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.service.ClusterProvider;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.event.LeaderBoardGlobalEvent;
import com.tarantula.platform.leaderboard.LeaderBoardEntry;
import com.tarantula.platform.leaderboard.LeaderBoardSync;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.ServiceProvider;

import java.util.concurrent.ConcurrentHashMap;

/**
 * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
 * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
 * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
 * zxp = zxp +xp-delta
 * xp = xp + xp-delta
 */
public class GameServiceProvider implements ServiceProvider,LeaderBoard.Listener,DataStore.Listener{

    private JDKLogger logger = JDKLogger.getLogger(GameServiceProvider.class);
    private final String NAME;
    private static int ELO_K = 30;
    private static int LDB_SIZE = 10;
    private DataStore dataStore;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();
    private EventService publisher;
    private String dest;
    private ClusterProvider integrationCluster;
    private ConcurrentHashMap<String,ZoneListener> zMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,Rating> rMap = new ConcurrentHashMap<>();
    private ServiceContext serviceContext;
    public GameServiceProvider(String name){
        NAME = name;
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
    public void addZoneListener(String key,ZoneListener zoneListener){
        zMap.put(key,zoneListener);
    }
    public void removeZoneListener(String key){
        zMap.remove(key);
    }
    public Zone zone(Descriptor descriptor){//application id
        Zone zone = new Zone();
        zone.distributionKey(descriptor.distributionKey());
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
        return zone;
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
        this.dest = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).subscription();
        serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).addEventListener(NAME,(e)->{
            LeaderBoardEntry update = new LeaderBoardEntry(e.index(),e.name(),e.version(),e.owner(),e.balance(),e.timestamp());
            LeaderBoardSync ldb = this._leaderBoard(update.category());
            ldb.onView(update);
            return false;
        });
        integrationCluster = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
        this.dataStore.registerListener(new GamePortableRegistry().registryId(),this);
        logger.info("Game service provider ["+ NAME+"] started");
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
        integrationCluster.removeEventListener(NAME);
    }
    private double probability(double rating1,double rating2) {
        return 1.0 * 1.0 / (1 + 1.0 * (Math.pow(10, 1.0 * (rating1 - rating2) / 400)));
    }

    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        publisher.publish(new LeaderBoardGlobalEvent(dest,NAME,entry));
    }

    @Override
    public <T extends Recoverable> void onCreated(T t, String akey,byte[] key, byte[] value) {
        logger.warn("created->"+akey+"<><><>"+new String(value));
    }

    @Override
    public <T extends Recoverable> void onUpdated(T t, String akey,byte[] key, byte[] value) {
        logger.warn("updated->"+akey+"<><><>"+new String(value));
        this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).deployService().sync(NAME,t.getFactoryId(),t.getClassId(),akey,key,value);
        //serviceContext.clusterProvider(Distributable.DATA_SCOPE).deployService().distribute(t);
        //ZoneListener zl = zMap.get(t.distributionKey());
        //if(zl!=null){
            //zl.updated((Zone)t);
        //}
        //else{
            //logger.warn("Missed registered zone Listener->"+t.distributionKey());
        //}
    }
    @Override
    public void updateForData(int factoryId,int classId,String key,byte[] value){
        //Recoverable t = serviceContext.recoverableRegistry(factoryId).create(classId);
        //t.distributionKey();
        logger.warn("update for data key->"+key);
        logger.warn("update for data value->"+new String(value));
    }
}
