package com.tarantula.platform.service.cluster.presence;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.service.MetricsListener;

import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.presence.DistributionPresenceService;
import com.tarantula.platform.service.cluster.ClusterUtil;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class DistributionPresenceServiceProxy extends AbstractDistributedObject<PresenceClusterService> implements DistributionPresenceService {

    private String objectName;
    private MetricsListener metricsListener;

    public DistributionPresenceServiceProxy(String objectName, NodeEngine nodeEngine, PresenceClusterService clusterService){
        super(nodeEngine,clusterService);
        this.objectName = objectName;
    }
    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionPresenceService.NAME;
    }

    @Override
    public String name() {
        return DistributionPresenceService.NAME;
    }

    @Override
    public int profileSequence(String serviceName, String name) {
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(name);
        ProfileNameSequenceOperation operation = new ProfileNameSequenceOperation(serviceName,name);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionPresenceService.NAME,operation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Integer> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (int)result.result;
    }

    @Override
    public boolean deleteUserLoginData(long playerID) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> members = nodeEngine.getClusterService().getMembers();
        DeleteUserLoginDataOperation operation = new DeleteUserLoginDataOperation(playerID);
        for(Member member : members) {
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionPresenceService.NAME, operation, member.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries, TarantulaContext.operationRejectInterval, () -> {
                Future<Boolean> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
            }, metricsListener);
            if (!callResult.successful) throw new RuntimeException(callResult.exception);
        }
        return true;
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
