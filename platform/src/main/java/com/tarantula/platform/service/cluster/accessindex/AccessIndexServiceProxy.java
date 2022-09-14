package com.tarantula.platform.service.cluster.accessindex;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;
import com.icodesoftware.AccessIndex;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.recover.DataStoreSyncBatchOperation;
import com.tarantula.platform.service.cluster.recover.DataStoreSyncEndOperation;
import com.tarantula.platform.service.cluster.recover.DataStoreSyncStartOperation;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class AccessIndexServiceProxy extends AbstractDistributedObject<AccessIndexClusterService> implements AccessIndexService, DistributedObject {


    private final String objectName;//unique proxy name

    public AccessIndexServiceProxy(String objectName, NodeEngine nodeEngine,AccessIndexClusterService accessIndexService){
        super(nodeEngine,accessIndexService);
        this.objectName = objectName;
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
        final Future<AccessIndex> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    @Override
    public AccessIndex setIfAbsent(String accessKey,int referenceId){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSetIfAbsentOperation operation = new AccessIndexSetIfAbsentOperation(accessKey,referenceId);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        final Future<AccessIndex> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void setup(ServiceContext serviceContext){}
    public void waitForData(){}
    @Override
    public AccessIndex get(String accessKey) {
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexGetOperation operation = new AccessIndexGetOperation(accessKey);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        final Future<AccessIndexTrack> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }

    public boolean onEnable(){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            AccessIndexServiceUpdateOperation operation = new AccessIndexServiceUpdateOperation(true);
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
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
            final Future<Void> future = builder.invoke();
            try {
                future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
    }

    public int onReplicate(int partition,byte[] key,byte[] value,int nodeNumber){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = nodeNumber;
        for(Member m :mlist){
            if(!m.localMember()){
                ReplicateOnIntegrationScopeOperation operation = new ReplicateOnIntegrationScopeOperation(partition,key,value);
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
                final Future<Void> future = builder.invoke();
                try {
                    future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                    expected--;
                    if(expected==0) break;
                } catch (Exception e) {
                    future.cancel(true);
                    //goes to next node if failed
                }
            }
        }
        return expected;
    }
    public byte[] onRecover(int partition,byte[] key){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        byte[] ret = null;
        for(Member m :mlist){
            if(!m.localMember()){
                AccessIndexRecoverOperation operation = new AccessIndexRecoverOperation(partition,key);
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
                final Future<byte[]> future = builder.invoke();
                try {
                    ret = future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                    if(ret!=null){
                        break;
                    }
                } catch (Exception e) {
                    future.cancel(true);
                    //goes to next node if failed
                }
            }
        }
        return ret;
    }

    public int onStartSync(int partition,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSyncStartOperation operation = new AccessIndexSyncStartOperation(nodeEngine.getLocalMember().getUuid(),partition,syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<Integer> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            //throw ExceptionUtil.rethrow(e);
            return -1;
        }
    }
    public void onSync(int size,byte[][] keys,byte[][] values,String memberId,int partition){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSyncBatchOperation operation = new AccessIndexSyncBatchOperation(size,keys,values,partition);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        try {
            final Future<Void> future = builder.invoke();
            future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public void onEndSync(String memberId,String syncKey){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSyncEndOperation operation = new AccessIndexSyncEndOperation(syncKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        try {
            final Future<Void> future = builder.invoke();
            future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
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


}
