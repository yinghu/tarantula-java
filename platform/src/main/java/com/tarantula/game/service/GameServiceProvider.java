package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.game.*;
import com.tarantula.platform.event.GameUpdateEvent;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;

import com.tarantula.platform.tournament.*;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameServiceProvider implements ServiceProvider{

    private TarantulaLogger logger;
    private final String NAME;

    private ConcurrentHashMap<String,Room> roomIndex = new ConcurrentHashMap<>();


    private String subscription;

    private ClusterProvider integrationCluster;
    private ClusterProvider dataCluster;
    private ServiceContext serviceContext;

    private DistributionRoomServiceProvider roomServiceProvider;
    private PlayerDataProvider playerDataProvider;
    private LeaderBoardProvider leaderBoardProvider;
    private ItemConfigurationServiceProvider configurationServiceProvider;
    private DistributedTournamentServiceProvider tournamentServiceProvider;


    public GameServiceProvider(String name){
        NAME = name;
    }

    public GameLobby lobby(Descriptor descriptor){
        return new GameLobby();
    }
    public GameZone zone(Descriptor descriptor){//application id
        DynamicLobbySetup dynamicLobbySetup = new DynamicLobbySetup();
        return dynamicLobbySetup.load(serviceContext,descriptor);
    }


    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(GameServiceProvider.class);
        this.serviceContext = serviceContext;
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
        this.roomServiceProvider = new DistributionRoomServiceProvider(NAME);
        this.roomServiceProvider.setup(serviceContext);
        this.roomServiceProvider.waitForData();
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
        this.roomServiceProvider.start();
        this.playerDataProvider.start();
        this.leaderBoardProvider.start();
        this.tournamentServiceProvider.start();
        this.configurationServiceProvider.start();
    }

    @Override
    public void shutdown() throws Exception {
        logger.warn("shut down service->"+NAME);
        this.roomServiceProvider.shutdown();
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

    //room service provider hool calls
    public GameRoomServiceProvider roomServiceProvider(){
        return roomServiceProvider;
    }
    public String onRegisterRoom(Arena arena,Rating rating){
        return roomServiceProvider.onRegister(arena,rating);
    }
    public GameRoom onJoinRoom(Arena arena,String roomId,String systemId){
        return roomServiceProvider.onJoin(arena,roomId,systemId);
    }
    public void onLeaveRoom(String roomId,String systemId){
        this.roomServiceProvider.onLeave(roomId,systemId);
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
    public Tournament onSchedule(Tournament.Schedule schedule) { //all nodes
        return this.tournamentServiceProvider.schedule(schedule);
    }
    public Tournament onTournament(String tournamentId){ //register node
        return this.tournamentServiceProvider.tournament(tournamentId);
    }

    public Tournament.Instance onInstance(String tournamentId,String instanceId){//play node
        return this.tournamentServiceProvider.instance(tournamentId,instanceId);
    }
    public Tournament.Instance onInstance(String instanceId) { //play node
        return this.tournamentServiceProvider.instance(instanceId);
    }

}
