package com.tarantula.platform.service.cluster;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;

import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.platform.service.RecoverService;
import com.tarantula.platform.service.ServiceContext;

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
                    ret = future.get(5,TimeUnit.SECONDS);
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
    public void replicate(String source,int partition,byte[] key,byte[] value){
        NodeEngine nodeEngine = getNodeEngine();
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int maxReplicationNode = 3;
        for(Member m :mlist){
            if(!m.localMember()){
                ReplicateOnDataScopeOperation operation = new ReplicateOnDataScopeOperation(source,partition,key,value);
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
                final Future<Void> future = builder.invoke();
                try {
                    future.get(5, TimeUnit.SECONDS);
                    maxReplicationNode--;
                    if(maxReplicationNode==0){
                        break;
                    }
                } catch (Exception e) {
                    future.cancel(true);
                    //goes to next node if failed
                }
            }
        }
    }
    public void batch(ReplicationData[] batch){

    }
}
