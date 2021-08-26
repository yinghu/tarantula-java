package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.game.*;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;
import com.tarantula.platform.tournament.*;

import java.util.concurrent.ConcurrentHashMap;

public class GameServiceProvider implements ServiceProvider{

    private TarantulaLogger logger;
    private final String NAME;

    private ServiceContext serviceContext;

    private DistributionRoomService distributionRoomService;
    private PlayerDataProvider playerDataProvider;
    private LeaderBoardProvider leaderBoardProvider;
    private ItemConfigurationServiceProvider configurationServiceProvider;
    private DistributedTournamentServiceProvider tournamentServiceProvider;
    private Configuration configuration;
    private GameCluster gameCluster;
    private ConcurrentHashMap<String, GameZone.RoomProxy> roomProxyIndex;

    public GameServiceProvider(GameCluster gameCluster){
        NAME = (String) gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    public GameLobby lobby(Descriptor descriptor){
        DynamicGameLobbySetup dynamicLobbySetup = new DynamicGameLobbySetup();
        return dynamicLobbySetup.load(serviceContext,descriptor);
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
        this.roomProxyIndex = new ConcurrentHashMap<>();
        this.serviceContext = serviceContext;
        this.distributionRoomService = serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionRoomService.NAME);

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
        logger.info("Game service provider ["+ NAME+"] started on game cluster ["+gameCluster.distributionKey()+"]");
    }
    @Override
    public void waitForData(){
        this.configuration = serviceContext.configuration("game-cluster-settings");
    }
    @Override
    public void atMidnight(){
        leaderBoardProvider.atMidnight();
        tournamentServiceProvider.atMidnight();
    }
    @Override
    public void start() throws Exception {
        //this.roomServiceProvider.start();
        this.playerDataProvider.start();
        this.leaderBoardProvider.start();
        this.tournamentServiceProvider.start();
        this.configurationServiceProvider.start();
    }

    @Override
    public void shutdown() throws Exception {
        logger.warn("shut down service->"+NAME);
        //this.roomServiceProvider.shutdown();
        this.playerDataProvider.shutdown();
        this.leaderBoardProvider.shutdown();
        this.tournamentServiceProvider.shutdown();
        this.configurationServiceProvider.shutdown();
    }
    public void registerRoomProxy(String zoneId, GameZone.RoomProxy roomProxy){
        roomProxyIndex.put(zoneId,roomProxy);
    }
    public void releaseRoomProxy(String zoneId){
        roomProxyIndex.remove(zoneId);
    }
    //room service provider hool calls
    public DistributionRoomService distributionRoomService(){
        return distributionRoomService;
    }
    public String onRegisterRoom(String zoneId,Rating rating){
        GameZone.RoomProxy proxy = roomProxyIndex.get(zoneId);
        return proxy.onRegister(rating);
    }
    public GameRoom onJoinRoom(Arena arena,String roomId,String systemId){
        GameZone.RoomProxy proxy = roomProxyIndex.get(arena.owner());
        return proxy.onJoin(arena,roomId,systemId);
    }
    public void onLeaveRoom(String zoneId,String roomId,String systemId){
        GameZone.RoomProxy proxy = roomProxyIndex.get(zoneId);
        proxy.onLeave(roomId,systemId);
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
    public Tournament.RaceBoard onRaceBoard(String instanceId){
        return this.tournamentServiceProvider.instance(instanceId).raceBoard();
    }

}
