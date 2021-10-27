package com.tarantula.platform.service.cluster.room;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.Rating;
import com.tarantula.platform.room.DistributionRoomService;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.room.GameRoomRegistry;

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
    public GameRoomRegistry register(String serviceName, String zoneId, Rating rating) {
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(zoneId);
        RoomRegisterOperation roomRegisterOperation = new RoomRegisterOperation(serviceName,zoneId,rating);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomRegisterOperation,partitionId);
        final Future<GameRoomRegistry> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public void release(String serviceName,String zoneId,String roomId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(zoneId);
        RoomReleaseOperation roomReleaseOperation = new RoomReleaseOperation(serviceName,zoneId,roomId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomReleaseOperation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
        }
    }
    public GameRoom join(String serviceName,String roomId, String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomJoinOperation roomJoinOperation = new RoomJoinOperation(serviceName,roomId,systemId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomJoinOperation,partitionId);
        final Future<GameRoom> future = builder.invoke();
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
        RoomLeaveOperation roomLeaveOperation = new RoomLeaveOperation(serviceName,roomId,systemId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomLeaveOperation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
        }
    }
    public boolean localManaged(String key){
        int pid = getNodeEngine().getPartitionService().getPartitionId(key);
        return getNodeEngine().getPartitionService().getPartition(pid).isLocal();
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
