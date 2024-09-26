package com.tarantula.platform.service.cluster.metrics;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.ClusterUtil;
import com.tarantula.platform.service.metrics.DistributionMetricsService;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DistributionMetricsServiceProxy extends AbstractDistributedObject<MetricsClusterService> implements DistributionMetricsService {

    private String objectName;

    private MetricsListener metricsListener;
    public DistributionMetricsServiceProxy(String objectName, NodeEngine nodeEngine, MetricsClusterService metricsClusterService){
        super(nodeEngine,metricsClusterService);
        this.objectName = objectName;
    }

    @Override
    public String getName() {
        return objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionMetricsService.NAME;
    }

    @Override
    public byte[][] onMonitor(String serviceName) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        byte[][] ret = new byte[mlist.size()][];
        int i = 0;
        for(Member m : mlist){
            ServiceViewOperation serviceViewOperation = new ServiceViewOperation(serviceName);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionMetricsService.NAME, serviceViewOperation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            ret[i++]= result.successful? (byte[]) result.result : new byte[0];
        }
        return ret;
    }
    public byte[][] onMetrics(String name,String category,String classifier){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        byte[][] ret = new byte[mlist.size()][];
        int i = 0;
        for(Member m : mlist){
            MetricsViewOperation serviceViewOperation = new MetricsViewOperation(name,category,classifier);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionMetricsService.NAME, serviceViewOperation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            ret[i++]= result.successful? (byte[]) result.result : new byte[0];
        }
        return ret;
    }
    public byte[][] onMetricsArchive(String name, String category, String classifier, LocalDateTime end){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        byte[][] ret = new byte[mlist.size()][];
        int i = 0;
        for(Member m : mlist){
            MetricsArchiveViewOperation serviceViewOperation = new MetricsArchiveViewOperation(name,category,classifier,end);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionMetricsService.NAME, serviceViewOperation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            ret[i++]= result.successful? (byte[]) result.result : new byte[0];
        }
        return ret;
    }

    @Override
    public String name() {
        return DistributionMetricsService.NAME;
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
