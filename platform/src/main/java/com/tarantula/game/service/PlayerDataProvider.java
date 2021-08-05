package com.tarantula.game.service;

import com.icodesoftware.*;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.statistics.StatisticsIndex;

import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataProvider implements ServiceProvider {

    private TarantulaLogger logger;
    private static int ELO_K = 30;

    private final String name;

    private DataStore dataStore;

    private ConcurrentHashMap<String,PlayerData> tMap = new ConcurrentHashMap<>();

    public PlayerDataProvider(String name){
        this.name = name;
    }
    public Rating rating(String systemId){
        //return rMap.computeIfAbsent(systemId,(k)->{
        Rating rating = new Rating();
        rating.distributionKey(systemId);
        this.dataStore.createIfAbsent(rating,true);
        rating.dataStore(this.dataStore);
        return rating;
        //});
    }
    public Statistics statistics(String systemId,LeaderBoardProvider leaderBoardProvider){
        StatisticsIndex deltaStatistics = new StatisticsIndex();
        deltaStatistics.distributionKey(systemId);
        deltaStatistics.dataStore(this.dataStore);
        this.dataStore.createIfAbsent(deltaStatistics,true);
        deltaStatistics.registerListener((entry -> {
            LeaderBoard leaderBoard = leaderBoardProvider.leaderBoard(entry.name());
            leaderBoard.onAllBoard(entry);
        }));
        return deltaStatistics;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(PlayerDataProvider.class);
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());//typeId_service

    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        logger.warn("player service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }


    @Override
    public void atMidnight(){

    }
    /**
     * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
     * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
     * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
     * zxp = zxp +xp-delta
     * xp = xp + xp-delta
     */
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
    private double probability(double rating1,double rating2) {
        return 1.0 * 1.0 / (1 + 1.0 * (Math.pow(10, 1.0 * (rating1 - rating2) / 400)));
    }
    private class PlayerData{
        public Rating rating;
        public Statistics statistics;
    }
}
