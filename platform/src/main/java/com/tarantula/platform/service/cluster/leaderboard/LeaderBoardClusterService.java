package com.tarantula.platform.service.cluster.leaderboard;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.service.PlatformGameServiceProvider;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.presence.leaderboard.LeaderBoardEntry;
import com.tarantula.platform.room.GameRoom;

import java.util.Properties;

public class LeaderBoardClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(LeaderBoardClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.tarantulaContext = TarantulaContext.getInstance();
        log.warn("Start leader board cluster service");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new DistributionLeaderBoardServiceProxy(objectName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public void onUpdateLeaderBoard(String serviceName, LeaderBoard.Entry leaderBoardEntry){
        PlatformGameServiceProvider gameServiceProvider = (PlatformGameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        gameServiceProvider.leaderBoardProvider().leaderBoardUpdated(leaderBoardEntry);
    }


}
