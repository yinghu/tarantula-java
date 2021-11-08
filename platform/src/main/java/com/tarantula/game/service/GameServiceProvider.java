package com.tarantula.game.service;

import com.icodesoftware.*;
import com.icodesoftware.service.*;
import com.tarantula.game.*;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.achievement.AchievementServiceProvider;
import com.tarantula.platform.inbox.InboxServiceProvider;
import com.tarantula.platform.inventory.InventoryServiceProvider;
import com.tarantula.platform.item.ItemServiceProvider;
import com.tarantula.platform.leaderboard.LeaderBoardProvider;
import com.tarantula.platform.presence.DailyLoginTrack;
import com.tarantula.platform.presence.PresenceServiceProvider;
import com.tarantula.platform.room.RoomServiceProvider;
import com.tarantula.platform.service.ApplicationPreSetup;
import com.tarantula.platform.service.ClusterConfigurationCallback;
import com.tarantula.platform.store.StoreServiceProvider;
import com.tarantula.platform.tournament.*;
import com.tarantula.platform.util.SystemUtil;


public class GameServiceProvider implements ServiceProvider{

    private TarantulaLogger logger;
    private final String NAME;

    private ServiceContext serviceContext;

    private RoomServiceProvider roomServiceProvider;

    private LeaderBoardProvider leaderBoardProvider;
    private InventoryServiceProvider inventoryServiceProvider;
    private ItemServiceProvider itemServiceProvider;
    private StoreServiceProvider storeServiceProvider;
    private AchievementServiceProvider achievementServiceProvider;
    private PlatformTournamentServiceProvider tournamentServiceProvider;
    private PresenceServiceProvider presenceServiceProvider;
    private InboxServiceProvider inboxServiceProvider;
    private Configuration configuration;
    private GameCluster gameCluster;
    private ApplicationPreSetup applicationPreSetup;


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
        this.serviceContext = serviceContext;
        this.applicationPreSetup = SystemUtil.applicationPreSetup((String) gameCluster.property(GameCluster.LOBBY_PRE_SETUP_NAME));
        this.inventoryServiceProvider = new InventoryServiceProvider(gameCluster);
        this.inventoryServiceProvider.setup(serviceContext);
        this.inventoryServiceProvider.waitForData();
        this.storeServiceProvider = new StoreServiceProvider(gameCluster,inventoryServiceProvider);
        this.storeServiceProvider.setup(serviceContext);
        this.storeServiceProvider.waitForData();
        this.leaderBoardProvider = new LeaderBoardProvider(NAME);
        this.leaderBoardProvider.setup(serviceContext);
        this.leaderBoardProvider.waitForData();
        this.presenceServiceProvider = new PresenceServiceProvider(gameCluster,this.inventoryServiceProvider);
        this.presenceServiceProvider.setup(serviceContext);
        this.presenceServiceProvider.waitForData();
        this.itemServiceProvider = new ItemServiceProvider(gameCluster);
        this.itemServiceProvider.setup(serviceContext);
        this.itemServiceProvider.waitForData();
        this.inboxServiceProvider = new InboxServiceProvider(gameCluster,inventoryServiceProvider);
        this.inboxServiceProvider.setup(serviceContext);
        this.inboxServiceProvider.waitForData();
        this.achievementServiceProvider = new AchievementServiceProvider(gameCluster,inventoryServiceProvider);
        this.achievementServiceProvider.waitForData();
        this.achievementServiceProvider.setup(serviceContext);
        this.tournamentServiceProvider = new PlatformTournamentServiceProvider(gameCluster,this.inventoryServiceProvider);
        this.tournamentServiceProvider.setup(serviceContext);
        this.tournamentServiceProvider.waitForData();
        this.roomServiceProvider = new RoomServiceProvider(gameCluster);
        this.roomServiceProvider.setup(serviceContext);
        this.roomServiceProvider.waitForData();
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
        this.itemServiceProvider.start();
        this.presenceServiceProvider.start();
        this.inboxServiceProvider.start();
        this.roomServiceProvider.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.presenceServiceProvider.shutdown();
        this.leaderBoardProvider.shutdown();
        this.tournamentServiceProvider.shutdown();
        this.itemServiceProvider.shutdown();
        this.roomServiceProvider.shutdown();
        this.logger.warn("Game service provider ["+NAME+"] shutting down");
    }

    //room service provider hool calls
    public RoomServiceProvider roomServiceProvider(){
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
    public PresenceServiceProvider presenceServiceProvider(){
        return this.presenceServiceProvider;
    }
    public InventoryServiceProvider inventoryServiceProvider(){
        return this.inventoryServiceProvider;
    }
    public StoreServiceProvider storeServiceProvider(){ return this.storeServiceProvider; }
    public InboxServiceProvider inboxServiceProvider() { return this.inboxServiceProvider; }
    //leader service provider hook calls
    public LeaderBoard leaderBoard(String category){
        return leaderBoardProvider.leaderBoard(category);
    }

    //configuration service provider hood calls
    public ItemServiceProvider itemServiceProvider(){
        return this.itemServiceProvider;
    }

    //Achievement service provider
    public AchievementServiceProvider achievementServiceProvider(){
        return achievementServiceProvider;
    }
    //tournament service provider hook calls
    public TournamentServiceProvider tournamentServiceProvider(){
        return this.tournamentServiceProvider;
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

    public boolean onTryScheduleTournament(String scheduleId){
        return this.tournamentServiceProvider.trySchedule(scheduleId);
    }
    public boolean onTournamentScheduleFinished(String scheduleId){
        return this.tournamentServiceProvider.finishSchedule(scheduleId);
    }

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
        return null;
    }
}
