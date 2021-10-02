package com.tarantula.game.service;

import com.icodesoftware.*;

import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.presence.DailyLoginTrack;
import com.tarantula.platform.statistics.StatisticsIndex;


public class PlayerDataProvider implements ServiceProvider {

    private TarantulaLogger logger;

    private final String name;
    private ServiceContext serviceContext;
    private DataStore dataStore;

    private int dailyLoginPendingHours;
    private int maxConsecutiveDays;
    private int maxRewardTier;

    public PlayerDataProvider(String name){
        this.name = name;
    }
    public Rating rating(String systemId){
        Rating rating = new Rating();
        rating.distributionKey(systemId);
        this.dataStore.createIfAbsent(rating,true);
        rating.dataStore(this.dataStore);
        return rating;
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
    public DailyLoginTrack checkDailyLogin(String systemId){
        DailyLoginTrack dailyLoginTrack = new DailyLoginTrack();
        dailyLoginTrack.distributionKey(systemId);
        dailyLoginTrack.dataStore(dataStore);
        this.dataStore.createIfAbsent(dailyLoginTrack,true);
        return dailyLoginTrack.checkDailyLogin(dailyLoginPendingHours,maxConsecutiveDays,maxRewardTier)?dailyLoginTrack:null;
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.logger = serviceContext.logger(PlayerDataProvider.class);
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());//typeId_service
    }
    @Override
    public void waitForData(){
        Configuration configuration = serviceContext.configuration("game-daily-login-settings");
        this.dailyLoginPendingHours =((Number)configuration.property("waitingTimeHours")).intValue();
        this.maxConsecutiveDays = ((Number)configuration.property("maxConsecutiveDays")).intValue();
        this.maxRewardTier = ((Number)configuration.property("maxRewardTiers")).intValue();
    }
    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        logger.warn("player data service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }


    @Override
    public void atMidnight(){

    }

}
