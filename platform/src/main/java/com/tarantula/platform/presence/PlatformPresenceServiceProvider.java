package com.tarantula.platform.presence;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.service.ConfigurationServiceProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Rating;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.leaderboard.PlatformLeaderBoardProvider;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.presence.saves.SavedGame;
import com.tarantula.platform.presence.saves.SavedGameIndex;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformPresenceServiceProvider implements ConfigurationServiceProvider, ClusterConfigurationCallback {
    private TarantulaLogger logger;
    private final String gameServiceName;
    private final GameCluster gameCluster;
    private ServiceContext serviceContext;
    private DataStore presenceDataStore;
    private ApplicationPreSetup applicationPreSetup;


    private int dailyLoginPendingHours;
    private int maxConsecutiveDays;
    private int maxRewardTier;
    private int recentlyPlayListSize;
    private int friendListSize;

    private PlayList recentlyPlayList;
    private ConcurrentHashMap<String,DailyGiveaway> dailyGiveaways;
    private PlatformInventoryServiceProvider inventoryServiceProvider;
    private DistributionItemService distributionItemService;

    public PlatformPresenceServiceProvider(GameCluster gameCluster, PlatformInventoryServiceProvider inventoryServiceProvider){
        this.gameServiceName = (String)gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        this.inventoryServiceProvider = inventoryServiceProvider;
    }

    @Override
    public String name() {
        return "presence";
    }

    @Override
    public void start() throws Exception {
        this.dailyGiveaways = new ConcurrentHashMap<>();
        this.recentlyPlayList = new PlayList(recentlyPlayListSize);
        this.recentlyPlayList.distributionKey(this.gameCluster.distributionKey());
        this.presenceDataStore.createIfAbsent(this.recentlyPlayList,true);
        this.recentlyPlayList.dataStore(this.presenceDataStore);
        logger.warn("Presence service provider started->"+gameServiceName);
    }

    @Override
    public void shutdown() throws Exception {

    }
    @Override
    public void waitForData(){
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject dailyReward = ((JsonElement)configuration.property("dailyReward")).getAsJsonObject();
        this.dailyLoginPendingHours = dailyReward.get("waitingTimeHours").getAsInt();
        this.maxConsecutiveDays = dailyReward.get("maxConsecutiveDays").getAsInt();
        this.maxRewardTier = dailyReward.get("maxRewardTiers").getAsInt();
        this.recentlyPlayListSize = ((Number)configuration.property("recentlyPlayListSize")).intValue();
        this.friendListSize = ((Number)configuration.property("friendListSize")).intValue();
    }
    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String)gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.presenceDataStore = this.applicationPreSetup.dataStore(serviceContext,gameCluster,name());
        this.distributionItemService = this.serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionItemService.NAME);
        this.logger = serviceContext.logger(PlatformPresenceServiceProvider.class);
        this.logger.warn("Presence service provider started on ->"+gameServiceName);
    }
    public void onFriendList(String systemId,String friendSystemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.presenceDataStore.createIfAbsent(playList,true);
        playList.playListIndex.push(friendSystemId);
        this.presenceDataStore.update(playList);
    }
    public void onPlay(String systemId){
        this.recentlyPlayList.playListIndex.push(systemId);
        this.recentlyPlayList.update();
    }
    public List<String> friendList(String systemId){
        PlayList playList = new PlayList(friendListSize);
        playList.distributionKey(systemId);
        this.presenceDataStore.createIfAbsent(playList,true);
        return playList.playListIndex.list(new ArrayList<>());
    }
    public List<String> recentlyPlayList(){
        return this.recentlyPlayList.playListIndex.list(new ArrayList<>());
    }

    public Profile profile(String systemId){
        Profile profile = new Profile();
        profile.displayName ="player";
        profile.iconUrl = "resource/portrait.png";
        profile.distributionKey(systemId);
        this.presenceDataStore.createIfAbsent(profile,true);
        profile.dataStore(this.presenceDataStore);
        return profile;
    }
    public Rating rating(String systemId){
        Rating rating = new Rating();
        rating.distributionKey(systemId);
        this.presenceDataStore.createIfAbsent(rating,true);
        rating.dataStore(this.presenceDataStore);
        return rating;
    }
    public Statistics statistics(String systemId, PlatformLeaderBoardProvider leaderBoardProvider){
        StatisticsIndex deltaStatistics = new StatisticsIndex();
        deltaStatistics.distributionKey(systemId);
        deltaStatistics.dataStore(this.presenceDataStore);
        this.presenceDataStore.createIfAbsent(deltaStatistics,true);
        deltaStatistics.registerListener((entry -> {
            LeaderBoard leaderBoard = leaderBoardProvider.leaderBoard(entry.name());
            leaderBoard.onAllBoard(entry);
        }));
        return deltaStatistics;
    }
    public DailyLoginTrack checkDailyLogin(String gameId){
        DailyLoginTrack dailyLoginTrack = new DailyLoginTrack();
        dailyLoginTrack.distributionKey(gameId);
        dailyLoginTrack.dataStore(presenceDataStore);
        this.presenceDataStore.createIfAbsent(dailyLoginTrack,true);
        boolean rewarded = dailyLoginTrack.checkDailyLogin(dailyLoginPendingHours,maxConsecutiveDays,maxRewardTier);
        return rewarded?dailyLoginTrack:null;
    }
    public List<DailyGiveaway> list(){
        ArrayList<DailyGiveaway> _items = new ArrayList<>();
        dailyGiveaways.forEach((k,v)-> _items.add(v));
        return _items;
    }
    public List<SavedGame> listSaves(String systemId,String deviceId,String deviceName){
        SavedGameIndex savedGameIndex = new SavedGameIndex();
        savedGameIndex.distributionKey(systemId);
        savedGameIndex.dataStore(this.presenceDataStore);
        this.presenceDataStore.createIfAbsent(savedGameIndex,true);
        return savedGameIndex.list(deviceId,deviceName);
    }
    public boolean redeem(String systemId,String gameId){
        DailyLoginTrack dailyLoginTrack = new DailyLoginTrack();
        dailyLoginTrack.distributionKey(gameId);
        dailyLoginTrack.dataStore(presenceDataStore);
        if(!this.presenceDataStore.load(dailyLoginTrack)) return false;
        if(!dailyLoginTrack.rewardPending || !dailyGiveaways.containsKey(dailyLoginTrack.rewardKey())) return false;
        dailyLoginTrack.rewardPending = !this.inventoryServiceProvider.redeem(systemId,dailyGiveaways.get(dailyLoginTrack.rewardKey()));
        dailyLoginTrack.update();
        return !dailyLoginTrack.rewardPending;
    }

    @Override
    public <T extends Configurable> void register(T t) {
        t.registered();
        this.distributionItemService.register(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }

    @Override
    public <T extends Configurable> void release(T t) {
        t.released();
        this.distributionItemService.release(gameServiceName,name(),t.configurationTypeId(),t.distributionKey());
    }


    public String registerConfigurableListener(Descriptor application,Configurable.Listener listener) {
        List<DailyGiveaway> items = applicationPreSetup.list(serviceContext,application,new DailygGiveawayObjectQuery("typeId/"+application.category()));
        items.forEach((a)-> {
            if(!application.disabled()) dailyGiveaways.put(a.name(),a);
        });
        return null;
    }

    @Override
    public boolean onRegister(String category, String itemId) {
        DailyGiveaway dailyGiveaway = new DailyGiveaway();
        dailyGiveaway.distributionKey(itemId);
        GameCluster _gc = serviceContext.deploymentServiceProvider().gameCluster(gameCluster.distributionKey());
        Descriptor app = _gc.serviceWithCategory(category);
        if(!applicationPreSetup.load(serviceContext,app,dailyGiveaway)){
            return false;
        }
        dailyGiveaways.put(dailyGiveaway.name(),dailyGiveaway);
        return true;
    }
    public boolean onRelease(String category,String itemId){
        String[] released ={null};
        dailyGiveaways.forEach((k,v)->{
            if(v.distributionKey().equals(itemId)) released[0]=k;
        });
        if(released[0]!=null) dailyGiveaways.remove(released[0]);
        return false;
    }
}
