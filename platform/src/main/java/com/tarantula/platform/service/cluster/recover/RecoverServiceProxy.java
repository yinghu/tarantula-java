package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;

import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;
import com.icodesoftware.service.*;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.metrics.PerformanceMetrics;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RecoverServiceProxy extends AbstractDistributedObject<ClusterRecoverService> implements RecoverService {

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
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        byte[] ret = null;
        for(Member m : mlist){
            if(!m.localMember()){
                RecoverOperation operation = new RecoverOperation(source,key);
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
                final Future<byte[]> future = builder.invoke();
                try {
                    ret = future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                    if(ret!=null){
                        break;
                    }
                } catch (Exception e) {
                    future.cancel(true);
                    metricsListener.onUpdated(PerformanceMetrics.PERFORMANCE_CLUSTER_OPERATION_TIMEOUT_COUNT,1);
                    //goes to next node if failed
                }
            }
        }
        return ret;


    }
    @Override
    public int onReplicate(String source,byte[] key,byte[] value,int nodeNumber){
        NodeEngine nodeEngine = getNodeEngine();
        int cz = nodeEngine.getClusterService().getSize();
        if(cz==1) return nodeNumber;
        int expected = cz>nodeNumber? nodeNumber : cz-1;
        for(int i=0;i<expected;i++){
            ClusterProvider.Node roundRobinNode = this.serviceContext.clusterProvider().roundRobinMember();
            if(roundRobinNode==null) break;
            Member m = nodeEngine.getClusterService().getMember(roundRobinNode.memberId());
            ReplicateOnDataScopeOperation operation = new ReplicateOnDataScopeOperation(source,key,value);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
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
    }
    public int onReplicate(OnReplication[] batch,int size,int nodeNumber){
        NodeEngine nodeEngine = getNodeEngine();
        int cz = nodeEngine.getClusterService().getSize();
        if(cz==1) return nodeNumber;
        int expected = cz>nodeNumber? nodeNumber : cz-1;
        for(int i=0;i<expected;i++){
            ClusterProvider.Node roundRobinNode = this.serviceContext.clusterProvider().roundRobinMember();
            if(roundRobinNode==null) break;
            Member m = nodeEngine.getClusterService().getMember(roundRobinNode.memberId());
            BatchReplicateOnDataScopeOperation operation = new BatchReplicateOnDataScopeOperation(batch,size);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
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
    }
    public int onStartSync(String source,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreSyncStartOperation operation = new DataStoreSyncStartOperation(nodeEngine.getLocalMember().getUuid(),source,syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Integer> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
            //return 0;
        }
    }
    public void onSync(int partition,byte[][] keys,byte[][] values,String memberId,String source){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreSyncBatchOperation operation = new DataStoreSyncBatchOperation(partition,keys,values,source);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        try {
            final Future<Void> future = builder.invoke();
            future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public void onEndSync(String memberId,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreSyncEndOperation operation = new DataStoreSyncEndOperation(syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        try {
            final Future<Void> future = builder.invoke();
            future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public String[] onListModules(){
        NodeEngine nodeEngine = getNodeEngine();
        ListModulesOperation operation = new ListModulesOperation();
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<String[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public byte[] onLoadModuleJarFile(String fileName){
        NodeEngine nodeEngine = getNodeEngine();
        LoadModuleJarFileOperation operation = new LoadModuleJarFileOperation(fileName);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }


}