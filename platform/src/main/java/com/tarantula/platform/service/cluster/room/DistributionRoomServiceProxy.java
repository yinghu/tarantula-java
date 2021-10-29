package com.tarantula.platform.service.cluster.room;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.tarantula.platform.room.GameRoom;
import com.tarantula.game.Rating;
import com.tarantula.platform.room.DistributionRoomService;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.room.GameRoomRegistry;
import com.tarantula.platform.room.RoomJoinStub;

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
    public RoomJoinStub register(String serviceName, String zoneId, Rating rating) {
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(zoneId);
        RoomRegisterOperation roomRegisterOperation = new RoomRegisterOperation(serviceName,zoneId,rating);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomRegisterOperation,partitionId);
        final Future<RoomJoinStub> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public void release(String serviceName,String zoneId,String roomId,String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(zoneId);
        RoomReleaseOperation roomReleaseOperation = new RoomReleaseOperation(serviceName,zoneId,roomId,systemId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomReleaseOperation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
        }
    }
    public void sync(String serviceName,String zoneId,String roomId,String[] joined){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(zoneId);
        RoomSyncOperation roomSyncOperation = new RoomSyncOperation(serviceName,zoneId,roomId,joined);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomSyncOperation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
        }
    }
    public GameRoom view(String serviceName,String roomId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomViewOperation roomViewOperation = new RoomViewOperation(serviceName,roomId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomViewOperation,partitionId);
        final Future<GameRoom> future = builder.invoke();
        try {
            return future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return null;
        }
    }
    public GameRoom join(String serviceName,String ticket,String roomId, String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomJoinOperation roomJoinOperation = new RoomJoinOperation(serviceName,ticket,roomId,systemId);
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
    public void create(String serviceName,String zoneId,String roomId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomCreateOperation roomCreateOperation = new RoomCreateOperation(serviceName,zoneId,roomId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomCreateOperation,partitionId);
        final Future<Void> future = builder.invoke();
        try {
            future.get(TarantulaContext.operationTimeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
        }
    }
    public void load(String serviceName,String roomId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomLoadOperation roomLoadOperation = new RoomLoadOperation(serviceName,roomId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomLoadOperation,partitionId);
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
