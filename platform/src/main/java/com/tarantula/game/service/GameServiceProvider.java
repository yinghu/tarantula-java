package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.game.*;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.PlatformAchievementServiceProvider;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.inbox.PlatformInboxServiceProvider;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.item.PlatformItemServiceProvider;
import com.tarantula.platform.leaderboard.PlatformLeaderBoardProvider;
import com.tarantula.platform.lobby.PlatformLobbyServiceProvider;
import com.tarantula.platform.presence.DailyLoginTrack;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;
import com.tarantula.platform.room.PlatformRoomServiceProvider;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;

import com.tarantula.platform.store.PlatformStoreServiceProvider;
import com.tarantula.platform.tournament.*;



public class GameServiceProvider implements ServiceProvider,MetricsListener{

    private TarantulaLogger logger;
    private final String NAME;

    private ServiceContext serviceContext;

    private PlatformLobbyServiceProvider lobbyServiceProvider;
    private PlatformRoomServiceProvider roomServiceProvider;

    private PlatformLeaderBoardProvider leaderBoardProvider;
    private PlatformInventoryServiceProvider inventoryServiceProvider;
    private PlatformItemServiceProvider itemServiceProvider;
    private PlatformStoreServiceProvider storeServiceProvider;
    private PlatformAchievementServiceProvider achievementServiceProvider;
    private PlatformTournamentServiceProvider tournamentServiceProvider;
    private PlatformPresenceServiceProvider presenceServiceProvider;
    private PlatformInboxServiceProvider inboxServiceProvider;

    private PlatformConfigurationServiceProvider configurationServiceProvider;
    private Configuration configuration;
    private GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private DataStore serviceDataStore;

    public GameServiceProvider(GameCluster gameCluster){
        NAME = (String) gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    public GameLobby lobby(Descriptor descriptor){
        return applicationPreSetup.load(descriptor);
    }
    public Configuration configuration(){
        return configuration;
    }
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(GameServiceProvider.class);
        gameCluster.setup(serviceContext);
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.serviceDataStore = this.applicationPreSetup.dataStore(gameCluster,"player");
        this.lobbyServiceProvider = new PlatformLobbyServiceProvider(gameCluster);
        this.lobbyServiceProvider.setup(serviceContext);
        this.lobbyServiceProvider.waitForData();
        this.inventoryServiceProvider = new PlatformInventoryServiceProvider(gameCluster);
        this.inventoryServiceProvider.setup(serviceContext);
        this.inventoryServiceProvider.waitForData();
        this.storeServiceProvider = new PlatformStoreServiceProvider(gameCluster,inventoryServiceProvider);
        this.storeServiceProvider.setup(serviceContext);
        this.storeServiceProvider.waitForData();
        this.leaderBoardProvider = new PlatformLeaderBoardProvider(NAME);
        this.leaderBoardProvider.setup(serviceContext);
        this.leaderBoardProvider.waitForData();
        this.presenceServiceProvider = new PlatformPresenceServiceProvider(gameCluster,this.inventoryServiceProvider);
        this.presenceServiceProvider.setup(serviceContext);
        this.presenceServiceProvider.waitForData();
        this.itemServiceProvider = new PlatformItemServiceProvider(gameCluster);
        this.itemServiceProvider.setup(serviceContext);
        this.itemServiceProvider.waitForData();
        this.inboxServiceProvider = new PlatformInboxServiceProvider(gameCluster,inventoryServiceProvider);
        this.inboxServiceProvider.setup(serviceContext);
        this.inboxServiceProvider.waitForData();
        this.achievementServiceProvider = new PlatformAchievementServiceProvider(gameCluster,inventoryServiceProvider);
        this.achievementServiceProvider.waitForData();
        this.achievementServiceProvider.setup(serviceContext);
        this.tournamentServiceProvider = new PlatformTournamentServiceProvider(gameCluster,this.inventoryServiceProvider);
        this.tournamentServiceProvider.setup(serviceContext);
        this.tournamentServiceProvider.waitForData();
        this.roomServiceProvider = new PlatformRoomServiceProvider(gameCluster);
        this.roomServiceProvider.setup(serviceContext);
        this.roomServiceProvider.waitForData();
        this.configurationServiceProvider = new PlatformConfigurationServiceProvider(gameCluster);
        this.configurationServiceProvider.setup(serviceContext);
        this.configurationServiceProvider.waitForData();
        logger.info("Game service provider ["+ NAME+"] started on game cluster ["+gameCluster.distributionKey()+"]");
    }
    @Override
    public void waitForData(){
        this.configuration = serviceContext.configuration("game-cluster-settings");
        this.gameCluster.setup();
    }
    @Override
    public void atMidnight(){
        leaderBoardProvider.atMidnight();
        tournamentServiceProvider.atMidnight();
    }
    @Override
    public void start() throws Exception {
        this.lobbyServiceProvider.start();
        this.inventoryServiceProvider.start();
        this.presenceServiceProvider.start();
        this.leaderBoardProvider.start();
        this.tournamentServiceProvider.start();
        this.itemServiceProvider.start();
        this.presenceServiceProvider.start();
        this.inboxServiceProvider.start();
        this.roomServiceProvider.start();
        this.configurationServiceProvider.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.lobbyServiceProvider.shutdown();
        this.presenceServiceProvider.shutdown();
        this.leaderBoardProvider.shutdown();
        this.tournamentServiceProvider.shutdown();
        this.itemServiceProvider.shutdown();
        this.roomServiceProvider.shutdown();
        this.configurationServiceProvider.shutdown();
        this.logger.warn("Game service provider ["+NAME+"] shutting down");
    }
    public DataStore serviceDataStore(){
        return this.serviceDataStore;
    }

    //room service provider hool calls
    public PlatformRoomServiceProvider roomServiceProvider(){
        return roomServiceProvider;
    }

    //player data service provider hook calls
    public Rating rating(String systemId){
        return presenceServiceProvider.rating(systemId);
    }
    public Statistics statistics(String systemId){
        return presenceServiceProvider.statistics(systemId,leaderBoardProvider);
    }
    public DailyLoginTrack dailyLogin(String systemId){
        return presenceServiceProvider.checkDailyLogin(systemId);
    }

    public PlatformLobbyServiceProvider lobbyServiceProvider() {
        return lobbyServiceProvider;
    }

    public PlatformPresenceServiceProvider presenceServiceProvider(){
        return this.presenceServiceProvider;
    }
    public PlatformInventoryServiceProvider inventoryServiceProvider(){
        return this.inventoryServiceProvider;
    }
    public PlatformStoreServiceProvider storeServiceProvider(){ return this.storeServiceProvider; }
    public PlatformInboxServiceProvider inboxServiceProvider() { return this.inboxServiceProvider; }
    //leader service provider hook calls
    public LeaderBoard leaderBoard(String category){
        return leaderBoardProvider.leaderBoard(category);
    }

    //configuration service provider hood calls
    public PlatformItemServiceProvider itemServiceProvider(){
        return this.itemServiceProvider;
    }

    //Achievement service provider
    public PlatformAchievementServiceProvider achievementServiceProvider(){
        return achievementServiceProvider;
    }
    //tournament service provider hook calls
    public PlatformTournamentServiceProvider tournamentServiceProvider(){
        return this.tournamentServiceProvider;
    }

    public PlatformConfigurationServiceProvider configurationServiceProvider(){return this.configurationServiceProvider;}


    public ClusterConfigurationCallback clusterConfigurationCallback(String serviceName){
        if(serviceName.equals(itemServiceProvider.name())){
            return itemServiceProvider;
        }
        if(serviceName.equals(presenceServiceProvider.name())){
            return presenceServiceProvider;
        }
        if(serviceName.equals(achievementServiceProvider.name())){
            return achievementServiceProvider;
        }
        if(serviceName.equals(storeServiceProvider.name())){
            return storeServiceProvider;
        }
        if(serviceName.equals(tournamentServiceProvider.name())){
            return tournamentServiceProvider;
        }
        if(serviceName.equals(lobbyServiceProvider.name())){
            return lobbyServiceProvider;
        }
        if(serviceName.equals(configurationServiceProvider.name())){
            return configurationServiceProvider;
        }
        return null;
    }

    public ConfigurationServiceProvider configurationServiceProvider(String name){
        if(name.equals("store")) return storeServiceProvider;
        if(name.equals("achievement")) return achievementServiceProvider;
        if(name.equals("giveaway")) return presenceServiceProvider;
        if(name.equals("lobby")) return lobbyServiceProvider;
        if(name.equals("tournament")) return tournamentServiceProvider;
        if(name.equals("data")) return configurationServiceProvider;
        return null;
    }

    @Override
    public void onUpdated(String s, double v) {
        this.gameCluster.onUpdated(s,v);
    }

}
