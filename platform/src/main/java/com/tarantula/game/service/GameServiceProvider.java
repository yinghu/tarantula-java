package com.tarantula.game.service;

import com.tarantula.DataStore;
import com.tarantula.Statistics;
import com.tarantula.game.Stub;
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
    private static double BASE_POINTS = 100;
    private static int ELO_K = 30;
    private DataStore dataStore;
    public GameServiceProvider(String name){
        NAME = name;
    }
    public Rating xp(Stub stub){
        Rating rating = this.rating(stub.owner());
        double dxp = (1/stub.rank+stub.pxp)*BASE_POINTS;
        if(rating.csw>0){
            dxp = dxp+(rating.csw+1)*BASE_POINTS;
            rating.csw++;
        }
        rating.zxp += dxp;
        rating.xp += dxp;
        this.dataStore.update(rating);
        return rating;
    }
    public Rating rating(String systemId){
        Rating rating = new Rating();
        rating.distributionKey(systemId);
        this.dataStore.createIfAbsent(rating,true);
        return rating;
    }
    public void elo(Stub stub1,Stub stub2){
        Rating rating1 = this.rating(stub1.owner());
        Rating rating2 = this.rating(stub2.owner());
        double p1 = probability(rating2.elo,rating1.elo);
        double p2 = probability(rating1.elo,rating2.elo);
        if (stub1.rank-stub2.rank>0) {//1 win
            rating1.elo = rating1.elo + ELO_K * (1 - p1);
            rating2.elo = rating2.elo + ELO_K * (0 - p2);
        }
        else {//2 win
            rating1.elo = rating1.elo + ELO_K * (0 - p1);
            rating2.elo = rating2.elo + ELO_K * (1 - p2);
        }
        this.dataStore.update(rating1);
        this.dataStore.update(rating2);
    }
    public Statistics statistics(String systemId){
        DeltaStatistics deltaStatistics = new DeltaStatistics();
        deltaStatistics.distributionKey(systemId);
        deltaStatistics.dataStore(dataStore);

        return deltaStatistics;
    }
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        logger.warn("set up service service->"+NAME);
        this.dataStore = serviceContext.dataStore(NAME,serviceContext.partitionNumber());
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
