package com.tarantula.game.service;

import com.tarantula.*;
import com.tarantula.game.GamePortableRegistry;
import com.tarantula.game.Zone;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.presence.PresencePortableRegistry;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.event.LeaderBoardGlobalEvent;
import com.tarantula.platform.leaderboard.LeaderBoardEntry;
import com.tarantula.platform.leaderboard.LeaderBoardSync;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.ServiceProvider;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
 * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
 * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
 * zxp = zxp +xp-delta
 * xp = xp + xp-delta
 */
public class GameServiceProvider implements ServiceProvider,LeaderBoard.Listener{

    private JDKLogger logger = JDKLogger.getLogger(GameServiceProvider.class);
    private final String NAME;
    private static int ELO_K = 30;
    private static int LDB_SIZE = 10;
    private DataStore dataStore;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();
    private EventService publisher;
    private String dest;
    public GameServiceProvider(String name){
        NAME = name;
    }

    public Rating rating(String systemId){
        Rating rating = new Rating();
        rating.distributionKey(systemId);
        this.dataStore.createIfAbsent(rating,true);
        rating.dataStore(this.dataStore);
        return rating;
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

    public Zone zone(String zoneId){
        Zone zone = new Zone();
        zone.distributionKey(zoneId);
        this.dataStore.createIfAbsent(zone,true);
        zone.dataStore(this.dataStore);
        logger.warn(zone.toString());
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
        this.dataStore = serviceContext.dataStore(NAME,serviceContext.partitionNumber());
        this.publisher = serviceContext.eventService(Distributable.INTEGRATION_SCOPE);
        this.dest = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).subscription();
        serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE).addEventListener(NAME,(e)->{
            LeaderBoardEntry update = new LeaderBoardEntry(e.index(),e.name(),e.version(),e.owner(),e.balance(),e.timestamp());
            LeaderBoardSync ldb = this._leaderBoard(update.category());
            ldb.onView(update);
            return false;
        });

        this.dataStore.registerRecoverableListener(new GamePortableRegistry()).addRecoverableFilter(GamePortableRegistry.RATING_CID,(r)->{
            logger.warn(r.toString());
        });

        //this.dataStore.registerRecoverableListener(new PresencePortableRegistry()).addRecoverableFilter(PresencePortableRegistry.LEADER_BOARD_ENTRY_CID,(r)->{
            //logger.warn("DS->"+r.key().asString());
            //logger.warn("LD->"+r.toString());
        //});
         /**
        RecoverableListener c = this.dataStore.registerRecoverableListener(new PresencePortableRegistry());
        c.addRecoverableFilter(PresencePortableRegistry.STATISTICS_CID,(r)->{
            //logger.warn("DS->"+r.key().asString());
            logger.warn("DS->"+r.toString());
        });
        c.addRecoverableFilter(PresencePortableRegistry.STATISTICS_ENTRY_CID,(r)->{
           // logger.warn("EN->"+r.key().asString());
            logger.warn("EN->"+r.toString());
        });**/
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

    }
    private double probability(double rating1,double rating2) {
        return 1.0 * 1.0 / (1 + 1.0 * (Math.pow(10, 1.0 * (rating1 - rating2) / 400)));
    }

    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        publisher.publish(new LeaderBoardGlobalEvent(dest,NAME,entry));
    }
}
