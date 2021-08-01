package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.*;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.event.GameUpdateEvent;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;
import com.tarantula.platform.service.deployment.TypedListener;
import com.tarantula.platform.statistics.StatisticsIndex;
import com.tarantula.platform.event.LeaderBoardGlobalEvent;
import com.tarantula.platform.leaderboard.LeaderBoardEntry;
import com.tarantula.platform.leaderboard.LeaderBoardSync;
import com.tarantula.platform.tournament.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
 * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
 * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
 * zxp = zxp +xp-delta
 * xp = xp + xp-delta
 */
public class GameServiceProvider implements ServiceProvider{

    private TarantulaLogger logger;
    private final String NAME;

    private DataStore dataStore;


    private ConcurrentHashMap<String,Room> roomIndex = new ConcurrentHashMap<>();


    private String subscription;

    private ClusterProvider integrationCluster;
    private ClusterProvider dataCluster;
    private ServiceContext serviceContext;

    private PlayerDataProvider playerDataProvider;
    private LeaderBoardProvider leaderBoardProvider;
    private ItemConfigurationServiceProvider configurationServiceProvider;
    private DistributedTournamentServiceProvider tournamentServiceProvider;


    public GameServiceProvider(String name){
        NAME = name;
    }

    public GameZone zone(Descriptor descriptor){//application id
        DynamicLobbySetup dynamicLobbySetup = new DynamicLobbySetup();
        return dynamicLobbySetup.load(serviceContext,descriptor);
    }
    public void addRoom(Room room){
        roomIndex.put(room.roomId,room);
    }
    public Room getRoom(String roomId){
        return roomIndex.get(roomId);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(GameServiceProvider.class);
        this.serviceContext = serviceContext;
        this.dataStore = serviceContext.dataStore(NAME.replace("-","_"),serviceContext.partitionNumber());//typeId_service
        this.subscription = UUID.randomUUID().toString();
        integrationCluster = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
        integrationCluster.subscribe(subscription,(e)->{
            if(e instanceof GameUpdateEvent){
                Room room = roomIndex.get(e.trackId());
                room.onUpdated(e.action(),e.payload());
            }
            return false;
        });
        this.dataCluster = serviceContext.clusterProvider(Distributable.DATA_SCOPE);
        this.playerDataProvider = new PlayerDataProvider(NAME);
        this.playerDataProvider.setup(serviceContext);
        this.playerDataProvider.waitForData();
        this.leaderBoardProvider = new LeaderBoardProvider(NAME);
        this.leaderBoardProvider.setup(serviceContext);
        this.leaderBoardProvider.waitForData();
        this.configurationServiceProvider = new ItemConfigurationServiceProvider(NAME);
        this.configurationServiceProvider.setup(serviceContext);
        this.configurationServiceProvider.waitForData();
        this.tournamentServiceProvider = new DistributedTournamentServiceProvider(NAME);
        this.tournamentServiceProvider.setup(serviceContext);
        this.tournamentServiceProvider.waitForData();
        logger.info("Game service provider ["+ NAME+"] started on ["+subscription+"]");
    }
    @Override
    public void atMidnight(){
        leaderBoardProvider.atMidnight();
    }
    @Override
    public void start() throws Exception {
        this.playerDataProvider.start();
        this.leaderBoardProvider.start();
        this.tournamentServiceProvider.start();
        this.configurationServiceProvider.start();
    }

    @Override
    public void shutdown() throws Exception {
        logger.warn("shut down service->"+NAME);
        this.playerDataProvider.shutdown();
        this.leaderBoardProvider.shutdown();
        this.tournamentServiceProvider.shutdown();
        this.configurationServiceProvider.shutdown();
        this.dataCluster.unregisterReloadListener(name());
        integrationCluster.unsubscribe(NAME);
    }

    public void onClosed(Connection connection) {
        roomIndex.forEach((k,r)->r.connectionClosed(connection));
    }

    //player data service provider hook calls
    public Rating rating(String systemId){
        return playerDataProvider.rating(systemId);
    }
    public Statistics statistics(String systemId){
        return playerDataProvider.statistics(systemId,leaderBoardProvider);
    }

    //leader service provider hook calls
    public LeaderBoard leaderBoard(String category){
        return leaderBoardProvider.leaderBoard(category);
    }

    //configuration service provider hood calls
    public ConfigurationServiceProvider configurationServiceProvider(){
        return this.configurationServiceProvider;
    }
    public boolean onRegister(Configurable configurable){
        return configurationServiceProvider.onRegister(configurable);
    }

    //tournament service provider hook calls
    public TournamentServiceProvider tournamentServiceProvider(){
        return this.tournamentServiceProvider;
    }
    public Tournament onSchedule(Tournament.Schedule schedule) {
        return this.tournamentServiceProvider.schedule(schedule);
    }
    public Tournament onTournament(String tournamentId){
        return this.tournamentServiceProvider.tournament(tournamentId);
    }

    public Tournament.Instance onInstance(String tournamentId,String instanceId){
        return this.tournamentServiceProvider.instance(tournamentId,instanceId);
    }
    public Tournament.Instance onInstance(String instanceId) {
        return this.tournamentServiceProvider.instance(instanceId);
    }

}
