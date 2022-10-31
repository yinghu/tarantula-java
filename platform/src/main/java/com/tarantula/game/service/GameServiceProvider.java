package com.tarantula.game.service;

import com.google.gson.JsonElement;
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
import com.tarantula.platform.item.ItemDistributionCallback;

import com.tarantula.platform.service.metrics.GameClusterMetrics;
import com.tarantula.platform.store.PlatformStoreServiceProvider;
import com.tarantula.platform.tournament.*;

import java.util.concurrent.ConcurrentHashMap;


public class GameServiceProvider implements ServiceProvider,MetricsListener,ItemDistributionCallback{

    private TarantulaLogger logger;
    private final String NAME;

    private ServiceContext serviceContext;


    private PlatformLeaderBoardProvider leaderBoardProvider;

    private Configuration configuration;
    private GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private DataStore serviceDataStore;
    private MetricsListener metricsListener;
    private Metrics metrics;

    private ConcurrentHashMap<String,ServiceProvider> gameServiceProviders;

    public GameServiceProvider(GameCluster gameCluster){
        NAME = (String) gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
        metricsListener = (k,v)->{};
        this.gameServiceProviders = new ConcurrentHashMap<>();
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
        this.configuration = serviceContext.configuration("game-cluster-settings");
        JsonElement sp = (JsonElement)this.configuration.property("systemServiceProviders");
        sp.getAsJsonArray().forEach((e)->{
            try{
                ServiceProvider serviceProvider = (ServiceProvider)Class.forName(e.getAsString()).getConstructor(GameCluster.class,GameServiceProvider.class).newInstance(gameCluster,this);
                serviceProvider.setup(serviceContext);
                serviceProvider.waitForData();
                gameServiceProviders.put(serviceProvider.name(),serviceProvider);
            }catch (Exception nex){
                throw new RuntimeException(nex);
            }
        });
        JsonElement gp = (JsonElement)this.configuration.property("gameServiceProviders");
        gp.getAsJsonArray().forEach((e)->{
            try{
                ServiceProvider serviceProvider = (ServiceProvider)Class.forName(e.getAsString()).getConstructor(GameCluster.class,GameServiceProvider.class).newInstance(gameCluster,this);
                serviceProvider.setup(serviceContext);
                serviceProvider.waitForData();
                gameServiceProviders.put(serviceProvider.name(),serviceProvider);
            }catch (Exception nex){
                throw new RuntimeException(nex);
            }
        });
        this.metrics = new GameClusterMetrics((String)gameCluster.property(GameCluster.GAME_SERVICE));
        this.metrics.setup(serviceContext);
        serviceContext.registerMetrics(metrics);
        this.metrics = serviceContext.metrics((String)gameCluster.property(GameCluster.GAME_SERVICE));
        this.serviceContext = serviceContext;
        this.applicationPreSetup = gameCluster.applicationPreSetup();//SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.serviceDataStore = this.applicationPreSetup.dataStore(gameCluster,"player");


        this.leaderBoardProvider = new PlatformLeaderBoardProvider(NAME);
        this.leaderBoardProvider.setup(serviceContext);
        this.leaderBoardProvider.waitForData();

        this.serviceContext.deploymentServiceProvider().register(gameCluster);
        logger.info("Game service provider ["+ NAME+"] started on game cluster ["+gameCluster.distributionKey()+"]");
    }
    @Override
    public void waitForData(){
        this.gameCluster.setup();
    }
    @Override
    public void atMidnight(){
        leaderBoardProvider.atMidnight();
        tournamentServiceProvider().atMidnight();
    }
    @Override
    public void start() throws Exception {
        this.leaderBoardProvider.start();

        gameServiceProviders.forEach((k,sp)->{
            try {
                sp.start();
            }catch (Exception nex){}
        });
    }

    @Override
    public void shutdown() throws Exception {

        this.leaderBoardProvider.shutdown();

        gameServiceProviders.forEach((k,sp)->{
            try {
                sp.shutdown();
            }catch (Exception nex){}
        });
        gameServiceProviders.clear();
        this.logger.warn("Game service provider ["+NAME+"] shutting down");
    }
    public DataStore serviceDataStore(){
        return this.serviceDataStore;
    }

    //room service provider hool calls
    public PlatformRoomServiceProvider roomServiceProvider(){
        return  (PlatformRoomServiceProvider) gameServiceProviders.get(PlatformRoomServiceProvider.NAME);
    }

    //player data service provider hook calls
    public Rating rating(String systemId){
        return presenceServiceProvider().rating(systemId);
    }
    public Statistics statistics(String systemId){
        return presenceServiceProvider().statistics(systemId,leaderBoardProvider);
    }
    public DailyLoginTrack dailyLogin(String systemId){
        return presenceServiceProvider().checkDailyLogin(systemId);
    }

    public PlatformLobbyServiceProvider lobbyServiceProvider() {
        return (PlatformLobbyServiceProvider) gameServiceProviders.get(PlatformLobbyServiceProvider.NAME);
    }

    public PlatformPresenceServiceProvider presenceServiceProvider(){
        return (PlatformPresenceServiceProvider) gameServiceProviders.get(PlatformPresenceServiceProvider.NAME);
    }
    public PlatformInventoryServiceProvider inventoryServiceProvider(){
        return (PlatformInventoryServiceProvider) gameServiceProviders.get(PlatformInventoryServiceProvider.NAME);
    }
    public PlatformStoreServiceProvider storeServiceProvider(){
        return (PlatformStoreServiceProvider) gameServiceProviders.get(PlatformStoreServiceProvider.NAME);
    }
    public PlatformInboxServiceProvider inboxServiceProvider() {
        return (PlatformInboxServiceProvider) gameServiceProviders.get(PlatformInboxServiceProvider.NAME);
    }
    //leader service provider hook calls
    public LeaderBoard leaderBoard(String category){
        return leaderBoardProvider.leaderBoard(category);
    }

    //configuration service provider hood calls
    public PlatformItemServiceProvider itemServiceProvider(){
        return  (PlatformItemServiceProvider) gameServiceProviders.get(PlatformItemServiceProvider.NAME);
    }

    //Achievement service provider
    public PlatformAchievementServiceProvider achievementServiceProvider(){
        return (PlatformAchievementServiceProvider) gameServiceProviders.get(PlatformAchievementServiceProvider.NAME);
    }
    //tournament service provider hook calls
    public PlatformTournamentServiceProvider tournamentServiceProvider(){
        return (PlatformTournamentServiceProvider) gameServiceProviders.get(PlatformTournamentServiceProvider.NAME);
    }

    public PlatformConfigurationServiceProvider configurationServiceProvider(){
        return (PlatformConfigurationServiceProvider) gameServiceProviders.get(PlatformConfigurationServiceProvider.NAME);
    }


    public ItemDistributionCallback clusterConfigurationCallback(String serviceName){
        if(serviceName.equals(PlatformItemServiceProvider.NAME)){
            return itemServiceProvider();
        }
        if(serviceName.equals(PlatformPresenceServiceProvider.NAME)){
            return presenceServiceProvider();
        }
        if(serviceName.equals(PlatformAchievementServiceProvider.NAME)){
            return achievementServiceProvider();
        }
        if(serviceName.equals(PlatformStoreServiceProvider.NAME)){
            return storeServiceProvider();
        }
        if(serviceName.equals(PlatformTournamentServiceProvider.NAME)){
            return tournamentServiceProvider();
        }
        if(serviceName.equals(PlatformLobbyServiceProvider.NAME)){
            return lobbyServiceProvider();
        }
        if(serviceName.equals(PlatformConfigurationServiceProvider.NAME)){
            return configurationServiceProvider();
        }
        return this;//default empty implementation
    }

    public ConfigurationServiceProvider configurationServiceProvider(String name){
        if(name.equals("store")) return storeServiceProvider();
        if(name.equals("achievement")) return achievementServiceProvider();
        if(name.equals("giveaway")) return presenceServiceProvider();
        if(name.equals("lobby")) return lobbyServiceProvider();
        if(name.equals("tournament")) return tournamentServiceProvider();
        if(name.equals("data")) return configurationServiceProvider();
        return null;
    }

    @Override
    public void onUpdated(String category, double delta) {
        metrics.onUpdated(category,delta);
        metricsListener.onUpdated(category,delta);
    }

    @Override
    public boolean onItemRegistered(String category, String itemId) {
        return false;
    }

    @Override
    public boolean onItemReleased(String category, String itemId) {
        return false;
    }
    public void registerMetricsListener(MetricsListener metricsListener){
        if(metricsListener== null) return;
        this.metricsListener = metricsListener;
    }
}
