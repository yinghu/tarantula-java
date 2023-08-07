package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.TarantulaContext;

import com.tarantula.platform.service.cluster.ClusterUtil;
import com.tarantula.platform.service.metrics.PerformanceMetrics;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AccessIndexServiceProxy extends AbstractDistributedObject<AccessIndexClusterService> implements AccessIndexService, DistributedObject {

    private TarantulaLogger logger = JDKLogger.getLogger(AccessIndexServiceProxy.class);
    private final String objectName;//unique proxy name
    private ServiceContext serviceContext;
    private MetricsListener metricsListener;

    private ServiceEventLogger serviceEventLogger;

    public AccessIndexServiceProxy(String objectName, NodeEngine nodeEngine,AccessIndexClusterService accessIndexService){
        super(nodeEngine,accessIndexService);
        this.objectName = objectName;
        this.metricsListener = (k,v)->{};
    }

    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return AccessIndexService.NAME;
    }

    @Override
    public AccessIndex set(String accessKey,int referenceId){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSetOperation operation = new AccessIndexSetOperation(accessKey,referenceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<AccessIndex> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
        return (AccessIndex)callResult.result;
    }
    @Override
    public AccessIndex setIfAbsent(String accessKey,int referenceId){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSetIfAbsentOperation operation = new AccessIndexSetIfAbsentOperation(accessKey,referenceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<AccessIndex> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
        return (AccessIndex)callResult.result;
    }
    @Override
    public void setup(ServiceContext serviceContext){
        this.serviceContext = serviceContext;
        this.serviceEventLogger = serviceContext.serviceEventLogger();
    }
    public void waitForData(){}
    @Override
    public AccessIndex get(String accessKey) {
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexGetOperation operation = new AccessIndexGetOperation(accessKey);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<AccessIndex> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
        return (AccessIndex)callResult.result;
    }

    public boolean onEnable(){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            AccessIndexServiceUpdateOperation operation = new AccessIndexServiceUpdateOperation(true);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            });
            if(callResult.successful) expected--;
        }
        return expected==0;
    }
    public boolean onDisable(){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            AccessIndexServiceUpdateOperation operation = new AccessIndexServiceUpdateOperation(false);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            });
            if(callResult.successful) expected--;
        }
        return expected==0;
    }
    public int onReplicate(OnReplication[] batch, int size, int nodeNumber){
        try{
            NodeEngine nodeEngine = getNodeEngine();
            int cz = nodeEngine.getClusterService().getSize();
            if(cz==1) return nodeNumber;
            int expected = cz>nodeNumber? nodeNumber : cz-1;
            for(int i=0;i<expected;i++){
                ClusterProvider.Node roundRobinNode = this.serviceContext.clusterProvider().roundRobinMember();
                if(roundRobinNode==null) break;
                Member m = nodeEngine.getClusterService().getMember(roundRobinNode.memberId());
                BatchReplicateOnIntegrationScopeOperation operation = new BatchReplicateOnIntegrationScopeOperation(batch,size);
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
                final Future<Void> future = builder.invoke();
                try {
                    future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                    expected--;
                } catch (Exception e) {
                    future.cancel(true);
                    //goes to next node if failed
                    metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_CLUSTER_OPERATION_TIMEOUT_COUNT,1);
                }
            }
            return expected;
        }catch (Exception ex){
            ex.printStackTrace();
            return nodeNumber;
        }
    }
    public int onReplicate(int partition, byte[] key, byte[] value,  ClusterProvider.Node[] nodes){
        NodeEngine nodeEngine = getNodeEngine();
        int replicated = 0;
        ReplicateOnIntegrationScopeOperation operation = new ReplicateOnIntegrationScopeOperation(partition,key,value);
        for(ClusterProvider.Node node : nodes){
            if(node==null) break;
            Member m = nodeEngine.getClusterService().getMember(node.memberId());
            if(m==null) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            });
            if(callResult.successful) replicated++;
        }
        return replicated;
    }
    public byte[] onRecover(int partition,byte[] key,ClusterProvider.Node[] nodes){
        NodeEngine nodeEngine = getNodeEngine();
        byte[] ret = null;
        AccessIndexRecoverOperation operation = new AccessIndexRecoverOperation(partition,key);
        for(ClusterProvider.Node node : nodes){
            if(node==null) continue; //next node
            Member m = nodeEngine.getClusterService().getMember(node.memberId());
            if(m==null) continue; //next node
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            });
            if(callResult.successful){
                ret = (byte[])callResult.result;
                break;
            }
        }
        return ret;
    }

    public int onStartSync(int partition,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSyncStartOperation operation = new AccessIndexSyncStartOperation(nodeEngine.getLocalMember().getUuid(),partition,syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,nodeEngine.getMasterAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Integer> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        return callResult.successful?(int)callResult.result:-1;
    }
    public void onSync(int size,byte[][] keys,byte[][] values,String memberId,int partition){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSyncBatchOperation operation = new AccessIndexSyncBatchOperation(size,keys,values,partition);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
    }
    public void onEndSync(String memberId,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSyncEndOperation operation = new AccessIndexSyncEndOperation(syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        });
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
    }
    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public String name() {
        return this.objectName;
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }

}
