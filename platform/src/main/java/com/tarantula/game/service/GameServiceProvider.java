package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.icodesoftware.util.JsonUtil;
import com.tarantula.game.*;
import com.tarantula.platform.ApplicationConfiguration;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;

import com.tarantula.platform.tournament.*;

import java.io.InputStream;

public class GameServiceProvider implements ServiceProvider{

    private TarantulaLogger logger;
    private final String NAME;

    private ServiceContext serviceContext;

    private DistributionRoomServiceProvider roomServiceProvider;
    private PlayerDataProvider playerDataProvider;
    private LeaderBoardProvider leaderBoardProvider;
    private ItemConfigurationServiceProvider configurationServiceProvider;
    private DistributedTournamentServiceProvider tournamentServiceProvider;
    private Configuration configuration;

    public GameServiceProvider(String name){
        NAME = name;
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
        this.serviceContext = serviceContext;
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
        logger.info("Game service provider ["+ NAME+"] started");
    }
    @Override
    public void waitForData(){
        this.configuration = serviceContext.configuration("game-cluster-settings");
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
