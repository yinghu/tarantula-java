package com.tarantula.platform.leaderboard;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.EventService;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.event.LeaderBoardGlobalEvent;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformLeaderBoardProvider implements ServiceProvider, LeaderBoard.Listener {

    private TarantulaLogger logger;
    private final String name;
    private static int LDB_SIZE = 10;
    private DataStore dataStore;

    private EventService publisher;

    private ClusterProvider integrationCluster;

    private ConcurrentHashMap<String, LeaderBoardSync> tMap = new ConcurrentHashMap<>();

    public PlatformLeaderBoardProvider(String name){
        this.name = name;
    }
    public LeaderBoardSync leaderBoard(String category){
        return tMap.computeIfAbsent(category,(s)->{
            LeaderBoardSync ldb = new LeaderBoardSync(category,LDB_SIZE);
            ldb.dataStore(this.dataStore);
            ldb.masterListener(this);
            ldb.load();
            return ldb;
        });
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.logger = serviceContext.logger(PlatformLeaderBoardProvider.class);
        this.dataStore = serviceContext.dataStore(name.replace("-","_"),serviceContext.partitionNumber());//typeId_service
        this.publisher = serviceContext.eventService(Distributable.INTEGRATION_SCOPE);
        integrationCluster = serviceContext.clusterProvider(Distributable.INTEGRATION_SCOPE);
        integrationCluster.subscribe(name,(e)->{
            if(e instanceof LeaderBoardGlobalEvent){
                LeaderBoardEntry update = new LeaderBoardEntry(e.index(),e.name(),e.version(),e.owner(),e.balance(),e.timestamp());
                LeaderBoardSync ldb = this.leaderBoard(update.category());
                ldb.onView(update);
            }
            return false;
        });
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void start() throws Exception {
        logger.warn("Leader board service provider started");
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void onUpdated(LeaderBoard.Entry entry) {
        publisher.publish(new LeaderBoardGlobalEvent(name,name,entry));
    }
    @Override
    public void atMidnight(){
        tMap.forEach((k,v)->{
            v.reset();
        });
    }
}
