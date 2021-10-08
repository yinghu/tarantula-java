package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.game.*;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.AchievementServiceProvider;
import com.tarantula.platform.inventory.InventoryServiceProvider;
import com.tarantula.platform.item.ItemConfigurationServiceProvider;
import com.tarantula.platform.leaderboard.LeaderBoardProvider;
import com.tarantula.platform.presence.DailyLoginTrack;
import com.tarantula.platform.presence.PresenceServiceProvider;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.tournament.*;
import com.tarantula.platform.util.SystemUtil;

import java.util.concurrent.ConcurrentHashMap;

public class GameServiceProvider implements ServiceProvider{

    private TarantulaLogger logger;
    private final String NAME;

    private ServiceContext serviceContext;

    private DistributionRoomService distributionRoomService;

    private LeaderBoardProvider leaderBoardProvider;
    private InventoryServiceProvider inventoryServiceProvider;
    private ItemConfigurationServiceProvider configurationServiceProvider;
    private AchievementServiceProvider achievementServiceProvider;
    private PlatformTournamentServiceProvider tournamentServiceProvider;
    private PresenceServiceProvider presenceServiceProvider;
    private Configuration configuration;
    private GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;
    private ConcurrentHashMap<String, GameZone.RoomProxy> roomProxyIndex;

    public GameServiceProvider(GameCluster gameCluster){
        NAME = (String) gameCluster.property(GameCluster.GAME_SERVICE);
        this.gameCluster = gameCluster;
    }

    public GameLobby lobby(Descriptor descriptor){
        return applicationPreSetup.load(serviceContext,descriptor);
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
        serviceContext.setup(gameCluster);
        this.roomProxyIndex = new ConcurrentHashMap<>();
        this.serviceContext = serviceContext;
        this.distributionRoomService = serviceContext.clusterProvider(Distributable.DATA_SCOPE).serviceProvider(DistributionRoomService.NAME);
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.inventoryServiceProvider = new InventoryServiceProvider(gameCluster);
        this.inventoryServiceProvider.setup(serviceContext);
        this.inventoryServiceProvider.waitForData();
        this.leaderBoardProvider = new LeaderBoardProvider(NAME);
        this.leaderBoardProvider.setup(serviceContext);
        this.leaderBoardProvider.waitForData();
        this.presenceServiceProvider = new PresenceServiceProvider(gameCluster,this.inventoryServiceProvider);
        this.presenceServiceProvider.setup(serviceContext);
        this.presenceServiceProvider.waitForData();
        this.configurationServiceProvider = new ItemConfigurationServiceProvider(gameCluster);
        this.configurationServiceProvider.setup(serviceContext);
        this.configurationServiceProvider.waitForData();
        this.achievementServiceProvider = new AchievementServiceProvider(gameCluster,inventoryServiceProvider);
        this.achievementServiceProvider.waitForData();
        this.achievementServiceProvider.setup(serviceContext);
        this.tournamentServiceProvider = new PlatformTournamentServiceProvider(gameCluster,this.inventoryServiceProvider);
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
        this.inventoryServiceProvider.start();
        this.presenceServiceProvider.start();
        this.leaderBoardProvider.start();
        this.tournamentServiceProvider.start();
        this.configurationServiceProvider.start();
        this.presenceServiceProvider.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.presenceServiceProvider.shutdown();
        this.leaderBoardProvider.shutdown();
        this.tournamentServiceProvider.shutdown();
        this.configurationServiceProvider.shutdown();
        this.logger.warn("Game service provider ["+NAME+"] shutting down");
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
        return presenceServiceProvider.rating(systemId);
    }
    public Statistics statistics(String systemId){
        return presenceServiceProvider.statistics(systemId,leaderBoardProvider);
    }
    public DailyLoginTrack dailyLogin(String systemId){
        return presenceServiceProvider.checkDailyLogin(systemId);
    }
    public PresenceServiceProvider presenceServiceProvider(){
        return this.presenceServiceProvider;
    }
    public InventoryServiceProvider inventoryServiceProvider(){
        return this.inventoryServiceProvider;
    }

    //leader service provider hook calls
    public LeaderBoard leaderBoard(String category){
        return leaderBoardProvider.leaderBoard(category);
    }

    //configuration service provider hood calls
    public ItemConfigurationServiceProvider configurationServiceProvider(){
        return this.configurationServiceProvider;
    }

    //Achievement service provider
    public AchievementServiceProvider achievementServiceProvider(){
        return achievementServiceProvider;
    }
    //tournament service provider hook calls
    public TournamentServiceProvider tournamentServiceProvider(){
        return this.tournamentServiceProvider;
    }
    public boolean onSchedule(Tournament.Schedule schedule) { //all nodes
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
