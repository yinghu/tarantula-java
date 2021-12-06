package com.tarantula.platform.service.cluster.recover;

import com.hazelcast.core.Member;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.AbstractDistributedObject;

import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;
import com.icodesoftware.Access;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.TarantulaContext;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RecoverServiceProxy extends AbstractDistributedObject<ClusterRecoverService> implements RecoverService {

    private String objectName;
    public RecoverServiceProxy(String objectName, NodeEngine nodeEngine, ClusterRecoverService clusterRecoverService){
        super(nodeEngine,clusterRecoverService);
        this.objectName = objectName;
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
    public boolean checkAccessControl(String systemId, Access.Role role){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        boolean ret = false;
        CheckAccessControlOperation operation = new CheckAccessControlOperation(systemId,role);
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
            final Future<Integer> future = builder.invoke();
            try {
                int flag = future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
                if(flag!=RecoverService.CHECK_SKIPPED){
                    ret = flag == RecoverService.ROLE_MATCHED;
                    break;
                }
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return ret;
    }
    public String findDataNode(String source,byte[] key){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        String ret = null;
        FindDataNodeOperation operation = new FindDataNodeOperation(source,key);
        for(Member m : mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
            final Future<String> future = builder.invoke();
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
        return ret;
    }
    public boolean queryStart(String memberId,String source,String dataStore,int factoryId,int classId,String[] params){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreQueryStartOperation operation = new DataStoreQueryStartOperation(nodeEngine.getLocalMember().getUuid(),source,dataStore,factoryId,classId,params);
        Address targetNode = memberId!=null?nodeEngine.getClusterService().getMember(memberId).getAddress():nodeEngine.getMasterAddress();
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,targetNode);
        try {
            final Future<Boolean> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public void query(String memberId,String source,byte[] key,byte[] value){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreQueryStreamOperation operation = new DataStoreQueryStreamOperation(source,key,value);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        try {
            final Future<Void> future = builder.invoke();
            future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public void queryEnd(String memberId,String source){
        NodeEngine nodeEngine = getNodeEngine();
        DataStoreQueryEndOperation operation = new DataStoreQueryEndOperation(source);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getClusterService().getMember(memberId).getAddress());
        try {
            final Future<Void> future = builder.invoke();
            future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public byte[] load(String memberId,String dataSource,byte[] key){
        NodeEngine nodeEngine = getNodeEngine();
        LoadOperation operation = new LoadOperation(dataSource,key);
        Address targetNode = memberId!=null?nodeEngine.getClusterService().getMember(memberId).getAddress():nodeEngine.getMasterAddress();
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,targetNode);
        try {
            final Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
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
    public byte[] loadModuleIndex(){
        NodeEngine nodeEngine = getNodeEngine();
        LoadModuleIndexOperation operation = new LoadModuleIndexOperation();
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    public byte[] loadGameClusterIndex(){
        NodeEngine nodeEngine = getNodeEngine();
        LoadGameClusterIndexOperation operation = new LoadGameClusterIndexOperation();
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<byte[]> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    @Override
    public byte[] findTypeIdIndex(String typeId) {
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        byte[] ret = null;
        for(Member m : mlist){
            if(!m.localMember()){
                FindTypeIdIndexOperation operation = new FindTypeIdIndexOperation(typeId);
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
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
}
