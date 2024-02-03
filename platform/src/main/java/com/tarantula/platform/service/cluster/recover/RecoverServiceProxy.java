package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;

import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.ClusterUtil;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RecoverServiceProxy extends AbstractDistributedObject<ClusterRecoverService> implements RecoverService,DistributionDataViewer {

    private TarantulaLogger logger = JDKLogger.getLogger(RecoverServiceProxy.class);
    private String objectName;


    private MetricsListener metricsListener;
    private ServiceContext serviceContext;

    public RecoverServiceProxy(String objectName, NodeEngine nodeEngine, ClusterRecoverService clusterRecoverService){
        super(nodeEngine,clusterRecoverService);
        this.objectName = objectName;
        this.metricsListener = (k,v)->{};
    }


    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return RecoverService.NAME;
    }

    @Override
    public String name() {
        return RecoverService.NAME;
    }

    @Override
    public void setup(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    @Override
    public void waitForData() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public byte[] onRecover(String source, byte[] key) {
        NodeEngine nodeEngine = getNodeEngine();
        byte[] ret = null;
        RecoverOperation operation = new RecoverOperation(source,key);
        Set<Member> members = nodeEngine.getClusterService().getMembers();
        for(Member member : members){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,member.getAddress());
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
    public Batchable onRecover(String source,String label,byte[] key,ClusterProvider.Node[] nodes){
        NodeEngine nodeEngine = getNodeEngine();
        Batchable ret = null;
        RecoverEdgeOperation operation = new RecoverEdgeOperation(source,label,key);
        for(ClusterProvider.Node node : nodes){
            Member m = nodeEngine.getClusterService().getMember(node.memberId());
            if(m==null) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<byte[]> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(callResult.successful && callResult.result!=null){
                ret = (Batchable) callResult.result;
                break;
            }
        }
        return ret;
    }
    public boolean onDelete(String source,byte[] key){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        DeleteOperation operation = new DeleteOperation(source,key);
        boolean expected = true;
        for(Member m : mlist){
            if(m.localMember()) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!callResult.successful){
                expected = false;
                break;
            }
        }
        return expected;
    }
    public boolean onDeleteEdge(String source,String label,byte[] key){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        DeleteEdgeFromLabelOperation operation = new DeleteEdgeFromLabelOperation(source,label,key);
        boolean expected = true;
        for(Member m : mlist){
            if(m.localMember()) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!callResult.successful){
                expected = false;
                break;
            }
        }
        return expected;
    }
    public boolean onDeleteEdge(String source,String label,byte[] key,byte[] edge){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        DeleteEdgeOperation operation = new DeleteEdgeOperation(source,label,key,edge);
        boolean expected = true;
        for(Member m : mlist){
            if(m.localMember()) continue;
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(!callResult.successful){
                expected = false;
                break;
            }
        }
        return expected;
    }
    @Override
    public int onReplicate(String nodeName,String source,String label, byte[] key, byte[] value, ClusterProvider.Node[] nodes){
        NodeEngine nodeEngine = getNodeEngine();
        int expected = 0;
        for(ClusterProvider.Node node : nodes){
            if(node==null) continue;
            Member m = nodeEngine.getClusterService().getMember(node.memberId());
            if(m==null) continue;
            ReplicateOnDataScopeOperation operation = new ReplicateOnDataScopeOperation(nodeName,source,label,key,value);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
            ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
                Future<Void> future = builder.invoke();
                return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
            },metricsListener);
            if(result.successful) expected++;
        }
        return expected;
    }

    public void onReplicate(String nodeName,OnReplication[] batch, int size, ClusterProvider.Node node){

        NodeEngine nodeEngine = getNodeEngine();
        Member m = nodeEngine.getClusterService().getMember(node.memberId());
        if(m==null) return;
        BatchReplicateOnDataScopeOperation operation = new BatchReplicateOnDataScopeOperation(nodeName,batch,size);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful){

        }
    }
    public int onStartSync(String source,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreSyncStartOperation operation = new DataStoreSyncStartOperation(nodeEngine.getLocalMember().getUuid(),source,syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Integer> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        return result.successful? (int) result.result:0;

    }
    public void onSync(int partition,byte[][] keys,byte[][] values,String memberId,String source){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreSyncBatchOperation operation = new DataStoreSyncBatchOperation(partition,keys,values,source);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
    }
    public void onEndSync(String memberId,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreSyncEndOperation operation = new DataStoreSyncEndOperation(syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
    }

    public String[] onListModules(){
        NodeEngine nodeEngine = getNodeEngine();
        ListModulesOperation operation = new ListModulesOperation();
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<String[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (String[]) result.result;
    }
    public byte[] onLoadModuleJarFile(String fileName){
        NodeEngine nodeEngine = getNodeEngine();
        LoadModuleJarFileOperation operation = new LoadModuleJarFileOperation(fileName);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (byte[]) result.result;
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }


    //DistributionDataViewer method
    @Override
    public byte[] load(String source, byte[] key, ClusterProvider.Node node) {
        NodeEngine nodeEngine = getNodeEngine();
        Member m = nodeEngine.getClusterService().getMember(node.memberId());
        if(m==null) return null;
        LoadDataOperation operation = new LoadDataOperation(source,key);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
        ClusterUtil.CallResult callResult = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        return callResult.successful?(byte[])callResult.result:null;
    }
}