package com.tarantula.platform.service.cluster.room;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.DistributionRoomService;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.recover.ClusterRecoverService;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DistributionRoomServiceProxy extends AbstractDistributedObject<RoomClusterService> implements DistributionRoomService {

    private String objectName;

    public DistributionRoomServiceProxy(String objectName, NodeEngine nodeEngine, RoomClusterService roomClusterService){
        super(nodeEngine,roomClusterService);
        this.objectName = objectName;
    }


    @Override
    public String getName() {
        return this.objectName;
    }

    @Override
    public String getServiceName() {
        return DistributionRoomService.NAME;
    }


    @Override
    public Stub join(String serviceName,Rating rating) {
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(rating.xpLevel);
        JoinOperation joinOperation = new JoinOperation(serviceName,rating);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME,joinOperation,partitionId);
        final Future<Stub> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public void leave(String serviceName,String roomId,String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        LeaveOperation leaveOperation = new LeaveOperation(serviceName,roomId,systemId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME,leaveOperation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
        }
    }

    @Override
    public String name() {
        return DistributionRoomService.NAME;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
