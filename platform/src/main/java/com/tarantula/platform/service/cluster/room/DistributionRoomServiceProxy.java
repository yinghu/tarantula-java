package com.tarantula.platform.service.cluster.room;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.service.MetricsListener;
import com.tarantula.platform.room.*;
import com.tarantula.platform.TarantulaContext;
import com.tarantula.platform.service.cluster.ClusterUtil;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DistributionRoomServiceProxy extends AbstractDistributedObject<RoomClusterService> implements DistributionRoomService {

    private String objectName;
    private MetricsListener metricsListener;
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


    public GameRoom onRoomView(String serviceName,String zoneId, String roomId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomViewOperation roomViewOperation = new RoomViewOperation(serviceName,zoneId,roomId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomViewOperation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<GameRoom> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (GameRoom) result.result;

    }
    public GameRoom onJoinRoom(String serviceName,String zoneId,String roomId, String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomJoinOperation roomJoinOperation = new RoomJoinOperation(serviceName,zoneId,roomId,systemId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomJoinOperation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<GameRoom> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        return (GameRoom) result.result;
    }

    public void onLeaveRoom(String serviceName,String zoneId,String roomId,String systemId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomLeaveOperation roomLeaveOperation = new RoomLeaveOperation(serviceName,zoneId,roomId,systemId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomLeaveOperation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
        //return (GameRoom) result.result;
    }

    public void onResetRoom(String serviceName,String zoneId,String roomId){
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(roomId);
        RoomResetOperation roomLeaveOperation = new RoomResetOperation(serviceName,zoneId,roomId);
        InvocationBuilder builder = nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME, roomLeaveOperation,partitionId);
        ClusterUtil.CallResult result = ClusterUtil.call(TarantulaContext.operationRetries,TarantulaContext.operationRejectInterval,()->{
            Future<Void> future = builder.invoke();
            return future.get(TarantulaContext.operationTimeout,TimeUnit.SECONDS);
        },metricsListener);
        if(!result.successful) throw new RuntimeException(result.exception);
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

    public void registerMetricsListener(MetricsListener metricsListener){
        this.metricsListener = metricsListener;
    }
    public void releaseMetricsListener(){
        this.metricsListener = (k,v)->{};
    }
}
