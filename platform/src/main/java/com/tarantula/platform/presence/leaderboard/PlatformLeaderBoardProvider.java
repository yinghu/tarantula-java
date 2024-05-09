package com.tarantula.platform.presence.leaderboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.icodesoftware.Configuration;
import com.icodesoftware.DataStore;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.EventService;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.ScheduleRunner;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.game.service.PlatformGameServiceSetup;
import com.tarantula.platform.event.LeaderBoardSyncEvent;
import com.tarantula.platform.presence.DistributionPresenceService;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformLeaderBoardProvider extends PlatformGameServiceSetup implements LeaderBoard.Listener {


    public static final String NAME = "leaderboard";


    private int leaderBoardSize = 10;
    private DataStore dataStore;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();

    private DistributionPresenceService distributionPresenceService;
    private String leaderBoardTopic;
    private EventService publisher;
    public PlatformLeaderBoardProvider(PlatformGameServiceProvider gameServiceProvider){
        super(gameServiceProvider,NAME);
        leaderBoardTopic = gameCluster.typeId()+"_"+NAME;
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
        this.distributionPresenceService = serviceContext.clusterProvider().serviceProvider(DistributionPresenceService.NAME);
        this.logger = JDKLogger.getLogger(PlatformLeaderBoardProvider.class);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void start() throws Exception {
        this.publisher = this.serviceContext.clusterProvider().subscribe(leaderBoardTopic,event -> {
            if(event instanceof LeaderBoardSyncEvent){
                LeaderBoard.Entry e = ((LeaderBoardSyncEvent)event).entry;
                logger.warn(e.category()+" : "+e.classifier()+" : "+e.systemId()+" : "+e.value()+" :"+e.timestamp());
                LeaderBoardSync leaderBoardSync = leaderBoard(e.category());
                switch (e.classifier()){
                    case LeaderBoard.DAILY:
                        leaderBoardSync.daily().onBoard(e);
                        break;
                    case LeaderBoard.WEEKLY:
                        leaderBoardSync.weekly().onBoard(e);
                        break;
                    case LeaderBoard.MONTHLY:
                        leaderBoardSync.monthly().onBoard(e);
                        break;
                    case LeaderBoard.YEARLY:
                        leaderBoardSync.yearly().onBoard(e);
                        break;
                    case LeaderBoard.TOTAL:
                        leaderBoardSync.total().onBoard(e);
                        break;
                }
            }
            return false;
        });
        logger.warn("Leader board service provider started on ["+gameCluster.serviceType()+"] with board size ["+leaderBoardSize+"]["+leaderBoardTopic+"]");
    }

    @Override
    public void shutdown() throws Exception {
        this.serviceContext.clusterProvider().unsubscribe(leaderBoardTopic);
    }

    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        distributionPresenceService.onUpdateLeaderBoard(gameServiceName,entry);
    }

    //distribution call
    public void onLeaderBoardUpdated(LeaderBoard.Entry entry){
        LeaderBoardSync sync = leaderBoard(entry.category());
        serviceContext.schedule(new ScheduleRunner(100,()->{
            sync.sync(entry,(e)->{
                /* callback on updated*/
                publisher.publish(new LeaderBoardSyncEvent(leaderBoardTopic,e));
            });
        }));
    }


    @Override
    public void atMidnight(){
        tMap.forEach((k,v)->{
            v.reset();
        });
    }

}
