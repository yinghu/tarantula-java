package com.tarantula.game.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.icodesoftware.protocol.GameServiceProxy;
import com.icodesoftware.service.*;
import com.tarantula.game.module.ErrorModule;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.PlatformAchievementServiceProvider;
import com.tarantula.platform.configuration.PlatformConfigurationServiceProvider;
import com.tarantula.platform.inbox.PlatformInboxServiceProvider;
import com.tarantula.platform.inventory.PlatformInventoryServiceProvider;
import com.tarantula.platform.leaderboard.PlatformLeaderBoardProvider;
import com.tarantula.platform.lobby.PlatformLobbyServiceProvider;
import com.tarantula.platform.messaging.PlatformMessagingServiceProvider;
import com.tarantula.platform.presence.PlatformPresenceServiceProvider;
import com.tarantula.platform.resource.PlatformResourceServiceProvider;
import com.tarantula.platform.room.PlatformRoomServiceProvider;
import com.tarantula.platform.item.ItemDistributionCallback;

import com.tarantula.platform.service.metrics.GameClusterMetrics;
import com.tarantula.platform.store.PlatformStoreServiceProvider;
import com.tarantula.platform.tournament.*;

import java.util.concurrent.ConcurrentHashMap;


public class PlatformGameServiceProvider implements MetricsListener,ItemDistributionCallback, ServiceProvider {

    private static final String CONFIG = "game-service-proxy-settings";

    private TarantulaLogger logger;
    private final String NAME;

    private ServiceContext serviceContext;


    private Configuration configuration;
    private final GameCluster gameCluster;

    private MetricsListener metricsListener;
    private Metrics metrics;

    private ConcurrentHashMap<String,ServiceProvider> gameServiceProviders;
    private ConcurrentHashMap<String,EventListener> eventListeners;
    private ConcurrentHashMap<String, Module> moduleExported;
    private ConcurrentHashMap<Short, GameServiceProxy> serviceExported;

    private Descriptor serviceProxy;

    public PlatformGameServiceProvider(GameCluster gameCluster){
        NAME = gameCluster.serviceType();
        this.gameCluster = gameCluster;
        metricsListener = (k,v)->{};
        this.gameServiceProviders = new ConcurrentHashMap<>();
        this.moduleExported = new ConcurrentHashMap<>();
        this.serviceExported = new ConcurrentHashMap<>();
        this.eventListeners = new ConcurrentHashMap<>();
    }

    public GameCluster gameCluster(){
        return gameCluster;
    }


    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(PlatformGameServiceProvider.class);
        gameCluster.setup(serviceContext);
        this.configuration = serviceContext.configuration("game-cluster-settings");
        JsonElement sp = (JsonElement)this.configuration.property("systemServiceProviders");
        sp.getAsJsonArray().forEach((e)->{
            try{
                ServiceProvider serviceProvider = (ServiceProvider)Class.forName(e.getAsString()).getConstructor(PlatformGameServiceProvider.class).newInstance(this);
                gameServiceProviders.put(serviceProvider.name(),serviceProvider);
            }catch (Exception nex){
                throw new RuntimeException(nex);
            }
        });
        JsonElement gp = (JsonElement)this.configuration.property("gameServiceProviders");
        gp.getAsJsonArray().forEach((e)->{
            try{
                ServiceProvider serviceProvider = (ServiceProvider)Class.forName(e.getAsString()).getConstructor(PlatformGameServiceProvider.class).newInstance(this);
                gameServiceProviders.put(serviceProvider.name(),serviceProvider);
            }catch (Exception nex){
                throw new RuntimeException(nex);
            }
        });
        gameServiceProviders.forEach((k,p)->{
            p.setup(serviceContext);
            p.waitForData();
        });
        Configuration config = serviceContext.configuration(CONFIG);
        JsonArray proxies = ((JsonElement)config.property("proxies")).getAsJsonArray();
        proxies.forEach((proxy->{
            JsonObject cc = proxy.getAsJsonObject();
            short serviceId = cc.get("serviceId").getAsShort();
            String className = cc.get("className").getAsString();
            GameServiceProxy serviceProxy = toGameServiceProxy(serviceId,className);
            serviceExported.put(serviceId,serviceProxy);
        }));
        this.metrics = new GameClusterMetrics((String)gameCluster.property(GameCluster.GAME_SERVICE));
        this.metrics.setup(serviceContext);
        serviceContext.registerMetrics(metrics);
        this.metrics = serviceContext.metrics((String)gameCluster.property(GameCluster.GAME_SERVICE));
        this.serviceContext = serviceContext;
        this.serviceContext.deploymentServiceProvider().register(gameCluster);
        this.serviceContext.clusterProvider().subscribe(gameCluster.typeId(),e->{
            EventListener listener = eventListeners.get(e.trackId());
            if(listener==null) return false;
            listener.onEvent(e);
            return true;
        });
        logger.info("Game service provider ["+ NAME+"] started on game cluster ["+gameCluster.distributionKey()+"]");
    }
    @Override
    public void waitForData(){
        this.gameCluster.setup();
    }

    @Override
    public void atMidnight(){
        gameServiceProviders.forEach((k,sp)->{
            try {
                sp.atMidnight();
            }catch (Exception nex){
                logger.error("error on service start",nex);
            }
        });
    }
    @Override
    public void start() throws Exception {
        gameServiceProviders.forEach((k,sp)->{
            try {
                sp.start();
            }catch (Exception nex){
                logger.error("error on service start",nex);
            }
        });
    }

    @Override
    public void shutdown() throws Exception {
        this.serviceContext.clusterProvider().unsubscribe(gameCluster.typeId());
        gameServiceProviders.forEach((k,sp)->{
            try {
                sp.shutdown();
            }catch (Exception nex){}
        });
        gameServiceProviders.clear();
        this.logger.warn("Game service provider ["+NAME+"] shutting down");
    }

    //room service provider hook calls
    public PlatformRoomServiceProvider roomServiceProvider(){
        return serviceProvider(PlatformRoomServiceProvider.NAME);
    }
    public PlatformLobbyServiceProvider lobbyServiceProvider() {
        return serviceProvider(PlatformLobbyServiceProvider.NAME);
    }

    public PlatformPresenceServiceProvider presenceServiceProvider(){
        return serviceProvider(PlatformPresenceServiceProvider.NAME);
    }
    public PlatformInventoryServiceProvider inventoryServiceProvider(){
        return serviceProvider(PlatformInventoryServiceProvider.NAME);
    }
    public PlatformStoreServiceProvider storeServiceProvider(){
        return serviceProvider(PlatformStoreServiceProvider.NAME);
    }
    public PlatformInboxServiceProvider inboxServiceProvider() {
        return serviceProvider(PlatformInboxServiceProvider.NAME);
    }
    public PlatformLeaderBoardProvider leaderBoardProvider() {
        return serviceProvider(PlatformLeaderBoardProvider.NAME);
    }
    //leader service provider hook calls
    public LeaderBoard leaderBoard(String category){
        return leaderBoardProvider().leaderBoard(category);
    }

    //configuration service provider hood calls
    //Achievement service provider
    public PlatformAchievementServiceProvider achievementServiceProvider(){
        return serviceProvider(PlatformAchievementServiceProvider.NAME);
    }
    //tournament service provider hook calls
    public PlatformTournamentServiceProvider tournamentServiceProvider(){
        return serviceProvider(PlatformTournamentServiceProvider.NAME);
    }

    public PlatformConfigurationServiceProvider configurationServiceProvider(){
        return serviceProvider(PlatformConfigurationServiceProvider.NAME);
    }


    public PlatformMessagingServiceProvider messagingServiceProvider(){
        return serviceProvider(PlatformMessagingServiceProvider.NAME);
    }

    public PlatformResourceServiceProvider resourceServiceProvider(){
        return serviceProvider(PlatformResourceServiceProvider.NAME);
    }

    public <T extends ServiceProvider> T serviceProvider(String name){
        return (T)gameServiceProviders.get(name);
    }

    public void exportServiceModule(String tag,Module serviceModule){
        moduleExported.putIfAbsent(tag,serviceModule);
    }

    public Module serviceModule(String module){
        return moduleExported.getOrDefault(module, ErrorModule.ERROR_MODULE);
    }

    public GameServiceProxy gameServiceProxy(short serviceId){
        return serviceExported.getOrDefault(serviceId,ErrorCommand.ERROR_COMMAND);
    }

    public ItemDistributionCallback clusterConfigurationCallback(String serviceName){
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
        if(serviceName.equals(PlatformResourceServiceProvider.NAME)){
            return resourceServiceProvider();
        }
        return this;//default empty implementation
    }

    public ConfigurationServiceProvider configurationServiceProvider(String name){
        if(name.equals(PlatformStoreServiceProvider.NAME)) return storeServiceProvider();
        if(name.equals(PlatformAchievementServiceProvider.NAME)) return achievementServiceProvider();
        if(name.equals("giveaway")) return presenceServiceProvider();
        if(name.equals(PlatformLobbyServiceProvider.NAME)) return lobbyServiceProvider();
        if(name.equals(PlatformTournamentServiceProvider.NAME)) return tournamentServiceProvider();
        if(name.equals("data")) return configurationServiceProvider();
        if(name.equals(PlatformResourceServiceProvider.NAME)) return resourceServiceProvider();
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

    public String registerEventListener(String trackId,EventListener eventListener){
        eventListeners.putIfAbsent(trackId,eventListener);
        return this.gameCluster.typeId();
    }

    public PlatformGameContext gameContext(Class module){
        return new PlatformGameContext(this.serviceContext,this,this.serviceContext.logger(module));
    }

    public void registerServiceProxyModule(Descriptor descriptor){
        this.serviceProxy = descriptor;
    }
    public Descriptor serviceProxy(){
        return this.serviceProxy;
    }

    private GameServiceProxy toGameServiceProxy(short serviceId,String className){
        try {
            GameServiceProxy serviceMessageListener = (GameServiceProxy) Class.forName(className).getConstructor(short.class, PlatformGameServiceProvider.class).newInstance(serviceId,this);
            return serviceMessageListener;
        }catch (Exception ex){
            this.logger.warn("Service Proxy ["+className+"] Without Implementation");
            return ErrorCommand.ERROR_COMMAND;
        }
    }
}
