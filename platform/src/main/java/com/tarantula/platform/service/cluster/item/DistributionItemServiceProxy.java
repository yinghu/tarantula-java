package com.tarantula.platform.service.cluster.item;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.item.DistributionItemService;
import com.tarantula.platform.service.cluster.ClusterUtil;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class DistributionItemServiceProxy extends AbstractDistributedObject<ItemClusterService> implements DistributionItemService {

    private String objectName;
    private MetricsListener metricsListener;
    public DistributionItemServiceProxy(String objectName, NodeEngine nodeEngine, ItemClusterService itemClusterService){
        super(nodeEngine,itemClusterService);
        this.objectName = objectName;
    }
    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionItemService.NAME;
    }

    @Override
    public boolean onRegisterItem(String gameServiceName,String serviceName,String category,String itemId) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        ItemRegisterOperation operation = new ItemRegisterOperation(gameServiceName,serviceName,category,itemId);
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionItemService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Boolean> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!result.successful) throw new RuntimeException(result.exception);
        }
        return true;
    }
    @Override
    public boolean onReleaseItem(String gameServiceName,String serviceName,String category,String itemId) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        ItemReleaseOperation operation = new ItemReleaseOperation(gameServiceName,serviceName,category,itemId);
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionItemService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Boolean> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!result.successful) throw new RuntimeException(result.exception);
        }
        return true;
    }
    public boolean onRegisterItem(String gameServiceName,String serviceName,int publishId,int configurationId){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        ConfigurationRegisteredOperation operation = new ConfigurationRegisteredOperation(gameServiceName,serviceName,publishId,configurationId);
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionItemService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Boolean> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!result.successful) throw new RuntimeException(result.exception);
        }
        return true;
    }
    public boolean onReleaseItem(String gameServiceName,String serviceName,int publishId,int configurationId){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        ConfigurationReleasedOperation operation = new ConfigurationReleasedOperation(gameServiceName,serviceName,publishId,configurationId);
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionItemService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Boolean> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!result.successful) throw new RuntimeException(result.exception);
        }
        return true;
    }
    @Override
    public String name() {
        return DistributionItemService.NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void setup(ServiceContext serviceContext){

    }
    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }
}
