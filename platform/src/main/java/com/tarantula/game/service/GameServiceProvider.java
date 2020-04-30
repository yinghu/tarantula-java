package com.tarantula.game.service;

import com.tarantula.DataStore;
import com.tarantula.Statistics;
import com.tarantula.game.Zone;
import com.tarantula.logging.JDKLogger;
import com.tarantula.platform.DeltaStatistics;
import com.tarantula.platform.service.ServiceContext;
import com.tarantula.platform.service.ServiceProvider;

/**
 * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
 * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
 * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
 * zxp = zxp +xp-delta
 * xp = xp + xp-delta
 */
public class GameServiceProvider implements ServiceProvider {

    private JDKLogger logger = JDKLogger.getLogger(GameServiceProvider.class);
    private final String NAME;
    private static int ELO_K = 30;
    private DataStore dataStore;
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
        DeltaStatistics deltaStatistics = new DeltaStatistics();
        deltaStatistics.distributionKey(systemId);
        this.dataStore.createIfAbsent(deltaStatistics,true);
        deltaStatistics.dataStore(this.dataStore);
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
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.dataStore = serviceContext.dataStore(NAME,serviceContext.partitionNumber());
        logger.info("Game service provider ["+ NAME+"] started");
    }

    @Override
    public void waitForData() {

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
}
