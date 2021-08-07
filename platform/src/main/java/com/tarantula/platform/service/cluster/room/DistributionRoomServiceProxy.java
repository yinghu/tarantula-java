package com.tarantula.platform.service.cluster.room;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.Rating;
import com.tarantula.game.service.DistributionRoomService;
import com.tarantula.platform.TarantulaContext;

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
    public String register(String serviceName, Arena arena, Rating rating) {
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(arena.level);
        RoomRegisterOperation roomJoinOperation = new RoomRegisterOperation(serviceName,arena,rating);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomJoinOperation,partitionId);
        final Future<String> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public GameRoom join(String serviceName,Arena arena,String roomId, String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomJoinOperation roomJoinOperation = new RoomJoinOperation(serviceName,arena,roomId,systemId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomJoinOperation,partitionId);
        final Future<GameRoom> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }

    public void leave(String serviceName,Arena arena,String roomId,String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(arena.level);
        RoomLeaveOperation roomLeaveOperation = new RoomLeaveOperation(serviceName,roomId,systemId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomLeaveOperation,partitionId);
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
