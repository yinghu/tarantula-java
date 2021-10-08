package com.tarantula.platform.presence;

import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.platform.inventory.InventoryServiceProvider;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.item.ConfigurableObjectQuery;
import com.tarantula.platform.leaderboard.LeaderBoardProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PresenceServiceProvider implements ConfigurationServiceProvider {
    private TarantulaLogger logger;
    private final String name;
    private final GameCluster gameCluster;
    private ServiceContext serviceContext;
    private DataStore dataStore;
    private ApplicationPreSetup applicationPreSetup;


    private int dailyLoginPendingHours;
    private int maxConsecutiveDays;
    private int maxRewardTier;
    private int recentlyPlayListSize;
    private int friendListSize;

    private PlayList recentlyPlayList;
    private ConcurrentHashMap<String,DailyGiveaway> dailyGiveaways;
    private InventoryServiceProvider inventoryServiceProvider;

    public PresenceServiceProvider(GameCluster gameCluster, InventoryServiceProvider inventoryServiceProvider){
        this.name = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        this.dailyGiveaways = new ConcurrentHashMap<>();
        this.recentlyPlayList = new PlayList(recentlyPlayListSize);
        this.recentlyPlayList.distributionKey(this.gameCluster.distributionKey());
        this.dataStore.createIfAbsent(this.recentlyPlayList,true);
        this.recentlyPlayList.dataStore(this.dataStore);
        logger.warn("presence service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void waitForData(){
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        this.dailyLoginPendingHours =((Number)configuration.property("waitingTimeHours")).intValue();
        this.maxConsecutiveDays = ((Number)configuration.property("maxConsecutiveDays")).intValue();
        this.maxRewardTier = ((Number)configuration.property("maxRewardTiers")).intValue();
        this.recentlyPlayListSize = ((Number)configuration.property("recentlyPlayListSize")).intValue();
        this.friendListSize = ((Number)configuration.property("friendListSize")).intValue();
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());
        this.logger = serviceContext.logger(PresenceServiceProvider.class);
    }
    public void onFriendList(String systemId,String friendSystemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.dataStore.createIfAbsent(playList,true);
        playList.playListIndex.push(friendSystemId);
        this.dataStore.update(playList);
    }
    public void onPlay(String systemId){
        this.recentlyPlayList.playListIndex.push(systemId);
        this.recentlyPlayList.update();
    }
    public List<String> friendList(String systemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.dataStore.createIfAbsent(playList,true);
        return playList.playListIndex.list(new ArrayList<>());
    }
    public List<String> recentlyPlayList(){
        return this.recentlyPlayList.playListIndex.list(new ArrayList<>());
    }

    public Rating rating(String systemId){
        Rating rating = new Rating();
        rating.distributionKey(systemId);
        this.dataStore.createIfAbsent(rating,true);
        rating.dataStore(this.dataStore);
        return rating;
    }
    public Statistics statistics(String systemId, LeaderBoardProvider leaderBoardProvider){
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
        boolean rewarded = dailyLoginTrack.checkDailyLogin(dailyLoginPendingHours,maxConsecutiveDays,maxRewardTier);
        if(rewarded){
            //redeem or inbox
            logger.warn("Rewarding key->"+dailyLoginTrack.rewardKey());
            DailyGiveaway dailyGiveaway = dailyGiveaways.get(dailyLoginTrack.rewardKey());
            this.logger.warn(dailyGiveaway.toJson().toString());
        }
        return rewarded?dailyLoginTrack:null;
    }

    public void redeem(String systemId){
        this.logger.warn("redeem daily reward->"+systemId);
        dailyGiveaways.forEach((a,v)->{
            inventoryServiceProvider.redeem(systemId,v);
        });
    }

    @Override
    public <T extends Configurable> void register(T t) {
        this.logger.warn(t.toJson().toString());
    }




    public String registerConfigurableListener(Descriptor application,Configurable.Listener listener) {
        List<DailyGiveaway> items = applicationPreSetup.list(serviceContext,application,new DailygGiveawayObjectQuery("category/"+application.category()));
        items.forEach((a)-> dailyGiveaways.put(a.name(),a));
        return null;
    }
}
