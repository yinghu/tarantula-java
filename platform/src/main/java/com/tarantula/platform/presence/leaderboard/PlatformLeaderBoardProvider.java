package com.tarantula.platform.presence.leaderboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.DataStore;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformLeaderBoardProvider extends PlatformGameServiceSetup implements LeaderBoard.Listener {


    public static final String NAME = "leaderboard";


    private int leaderBoardSize = 10;
    private DataStore dataStore;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();

    private DistributionLeaderBoardService distributionLeaderBoardService;

    public PlatformLeaderBoardProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
    }
    public LeaderBoardSync leaderBoard(String category){
        return tMap.computeIfAbsent(category,(s)->{
            LeaderBoardSync ldb = new LeaderBoardSync(category,leaderBoardSize,gameCluster.distributionId(),this);
            ldb.dataStore(this.dataStore);
            ldb.load();
            return ldb;
        });
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        Configuration configuration = serviceContext.configuration("game-presence-settings");
        JsonObject plist = ((JsonElement)configuration.property("leaderBoard")).getAsJsonObject();
        this.leaderBoardSize = plist.get("topListSize").getAsInt();
        this.dataStore = gameCluster.applicationPreSetup().dataStore(gameCluster,NAME);//typeId_service
        this.distributionLeaderBoardService = serviceContext.clusterProvider().serviceProvider(DistributionLeaderBoardService.NAME);
        this.logger = JDKLogger.getLogger(PlatformLeaderBoardProvider.class);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        logger.warn("Leader board service provider started on ["+gameCluster.serviceType()+"] with board size ["+leaderBoardSize+"]");
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        distributionLeaderBoardService.onUpdateLeaderBoard(gameServiceName,entry);
    }

    public void leaderBoardUpdated(LeaderBoard.Entry entry){
        LeaderBoardSync sync = leaderBoard(entry.category());
        serviceContext.schedule(new ScheduleRunner(100,()->{
            sync.sync(entry,(e)->{/* callback on updated*/});
        }));
    }
    @Override
    public void atMidnight(){
        tMap.forEach((k,v)->{
            v.reset();
        });
    }

}
