package com.tarantula.platform.service.cluster;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.Member;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.*;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.ServiceContext;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * updated by yinghu lu on 7/16/2020.
 */
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
    public AccessIndex set(String accessKey){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSetOperation operation = new AccessIndexSetOperation(accessKey);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        final Future<AccessIndex> future = builder.invoke();
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
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
            return future.get(5,TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }

    public boolean update(boolean state){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexServiceOperation operation = new AccessIndexServiceOperation(state);
        Set<Member> mlist = nodeEngine.getClusterService().getMembers();
        int expected = mlist.size();
        for(Member m :mlist){
            InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,m.getAddress());
            final Future<Void> future = builder.invoke();
            try {
                future.get(5, TimeUnit.SECONDS);
                expected--;
            } catch (Exception e) {
                future.cancel(true);
                //goes to next node if failed
            }
        }
        return expected==0;
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
