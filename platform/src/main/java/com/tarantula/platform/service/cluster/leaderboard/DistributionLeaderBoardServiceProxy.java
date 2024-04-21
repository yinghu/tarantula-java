package com.tarantula.platform.service.cluster.leaderboard;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.presence.leaderboard.DistributionLeaderBoardService;
import com.tarantula.platform.presence.leaderboard.LeaderBoardEntry;

import com.tarantula.platform.service.cluster.ClusterUtil;


import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DistributionLeaderBoardServiceProxy extends AbstractDistributedObject<LeaderBoardClusterService> implements DistributionLeaderBoardService {

    private String objectName;
    private MetricsListener metricsListener;
    public DistributionLeaderBoardServiceProxy(String objectName, NodeEngine nodeEngine, LeaderBoardClusterService leaderBoardClusterService){
        super(nodeEngine,leaderBoardClusterService);
        this.objectName = objectName;
    }

    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionLeaderBoardService.NAME;
    }


    public void onUpdateLeaderBoard(String serviceName, LeaderBoard.Entry leaderBoardEntry){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(leaderBoardEntry.category());
        LeaderBoardUpdateOperation leaderBoardUpdateOperation = new LeaderBoardUpdateOperation(serviceName,leaderBoardEntry);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionLeaderBoardService.NAME, leaderBoardUpdateOperation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
    }


    @Override
    public String name() {
        return DistributionLeaderBoardService.NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }
}
