package com.tarantula.platform.service.cluster;

import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;

import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;
import com.tarantula.platform.service.RecoverService;
import com.tarantula.platform.service.ServiceContext;

import java.util.Set;
import java.util.concurrent.Future;

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
        RecoverOperation operation = new RecoverOperation(source,key);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,nodeEngine.getMasterAddress());
        try {
            final Future<byte[]> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
        }
    }
    @Override
    public void replicate(String source,byte[] key,byte[] value){
        NodeEngine nodeEngine = getNodeEngine();
        ReplicateOperation operation = new ReplicateOperation(source,key,value);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        mlist.forEach((m)->{
            if(!m.localMember()){
                InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(RecoverService.NAME,operation,m.getAddress());
                try {
                    final Future<Void> future = builder.invoke();
                    future.get(); //retry if timeout
                } catch (Exception e) {
                    throw ExceptionUtil.rethrow(e);
                }
            }
        });
    }
}
