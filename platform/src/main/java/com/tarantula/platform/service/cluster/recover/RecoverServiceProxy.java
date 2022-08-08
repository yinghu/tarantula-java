package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;

import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.MetricsListener;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RecoverServiceProxy extends AbstractDistributedObject<ClusterRecoverService> implements RecoverService {

    private String objectName;

    private static TarantulaLogger logger = JDKLogger.getLogger(RecoverServiceProxy.class);

    private MetricsListener metricsListener;
    private MetricsListener metricsListenerHook;

    public RecoverServiceProxy(String objectName, NodeEngine nodeEngine, ClusterRecoverService clusterRecoverService){
        super(nodeEngine,clusterRecoverService);
        this.objectName = objectName;
        this.metricsListener = (k,v)->{
            if(metricsListenerHook != null) metricsListenerHook.onUpdated(k,v);
        };
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
    public byte[] recover(String source, byte[] key) {
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
                    metricsListener.onUpdated("1",1);
                    //goes to next node if failed
                }
            }
        }
        return ret;
    }
    @Override
    public int replicate(String source,int partition,byte[] key,byte[] value,int nodeNumber){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = nodeNumber;
        for(Member m :mlist){
            if(!m.localMember()){
                ReplicateOnDataScopeOperation operation = new ReplicateOnDataScopeOperation(source,partition,key,value);
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
                final Future<Void> future = builder.invoke();
                try {
                    future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                    expected--;
                    if(expected==0) break;
                } catch (Exception e) {
                    future.cancel(true);
                    logger.error("replicate error on node->"+m.getAddress(),e);
                    //goes to next node if failed
                }
            }
        }
        return expected;
    }
    public int syncStart(String source,String syncKey){
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
    public void sync(int partition,byte[][] keys,byte[][] values,String memberId,String source){
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
    public void syncEnd(String memberId,String syncKey){
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

    public String[] listModules(){
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
    public byte[] loadModuleJarFile(String fileName){
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
        this.metricsListenerHook = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListenerHook = null;
    }


}