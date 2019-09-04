package com.tarantula.platform.service.cluster;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.util.ExceptionUtil;
import com.tarantula.*;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.service.AccessIndexService;
import com.tarantula.platform.service.ServiceContext;

import java.util.concurrent.Future;

/**
 * updated by yinghu lu on 6/12/2018.
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
    public AccessIndex set(String accessKey,String bucket){
        NodeEngine nodeEngine = getNodeEngine();
        AccessIndexSetOperation operation = new AccessIndexSetOperation(accessKey,bucket);
        int partitionId = nodeEngine.getPartitionService().getPartitionId(accessKey);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(AccessIndexService.NAME,operation,partitionId);
        try {
            final Future<AccessIndex> future = builder.invoke();
            return future.get(); //retry if timeout
        } catch (Exception e) {
            throw ExceptionUtil.rethrow(e);
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
        try {
            final Future<AccessIndexTrack> future = builder.invoke();
            return future.get();
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
