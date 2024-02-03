package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;

import com.tarantula.platform.TarantulaContext;

import com.tarantula.platform.service.cluster.ClusterUtil;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AccessIndexServiceProxy extends AbstractDistributedObject<AccessIndexClusterService> implements AccessIndexService,DistributionAccessIndexViewer, DistributedObject {

    private TarantulaLogger logger = JDKLogger.getLogger(AccessIndexServiceProxy.class);
    private final String objectName;//unique proxy name

    private MetricsListener metricsListener;


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
        },metricsListener);
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
        },metricsListener);
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
        return (AccessIndex)callResult.result;
    }
    @Override
    public void setup(ServiceContext serviceContext){
        //this.serviceContext = serviceContext;
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
        },metricsListener);
        if(!callResult.successful){

            throw new RuntimeException(callResult.exception);
        }
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
            },metricsListener);
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
            },metricsListener);
            if(callResult.successful) expected--;
        }
        return expected==0;
    }
    public void onReplicate(String nodeName,OnReplication[] batch, int size, ClusterProvider.Node node){

        NodeEngine nodeEngine = getNodeEngine();
        Member m = nodeEngine.getClusterService().getMember(node.memberId());
        if(m==null) return;
        BatchReplicateOnIntegrationScopeOperation operation = new BatchReplicateOnIntegrationScopeOperation(nodeName,batch,size);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
    }
    public int onReplicate(String nodeName, byte[] key, byte[] value,  ClusterProvider.Node[] nodes){
        NodeEngine nodeEngine = getNodeEngine();
        int replicated = 0;
        ReplicateOnIntegrationScopeOperation operation = new ReplicateOnIntegrationScopeOperation(nodeName,key,value);
        for(ClusterProvider.Node node : nodes){
            if(node==null) break;
            Member m = nodeEngine.getClusterService().getMember(node.memberId());
            if(m==null) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(callResult.successful) replicated++;
        }
        return replicated;
    }
    public byte[] onRecover(String source,byte[] key){
        NodeEngine nodeEngine = getNodeEngine();
        byte[] ret = null;
        AccessIndexRecoverOperation operation = new AccessIndexRecoverOperation(source,key);
        Set<Member> members = nodeEngine.getClusterService().getMembers();
        for(Member member : members){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,member.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(callResult.successful && callResult.result != null){
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
        },metricsListener);
        return callResult.successful?(int)callResult.result:-1;
    }
    public void onSync(int size,byte[][] keys,byte[][] values,String memberId,int partition){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSyncBatchOperation operation = new AccessIndexSyncBatchOperation(size,keys,values,partition);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!callResult.successful) throw new RuntimeException(callResult.exception);
    }
    public void onEndSync(String memberId,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSyncEndOperation operation = new AccessIndexSyncEndOperation(syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
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


    //DistributionAccessIndexViewer methods
    @Override
    public byte[] load(String source, byte[] key, ClusterProvider.Node node) {
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexLoadOperation operation = new AccessIndexLoadOperation(source,key);
        Member m = nodeEngine.getClusterService().getMember(node.memberId());
        if(m==null) return null;
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        return callResult.successful?(byte[])callResult.result:null;
    }
}
