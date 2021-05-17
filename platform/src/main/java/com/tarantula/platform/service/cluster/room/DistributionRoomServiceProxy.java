package com.tarantula.platform.service.cluster.room;

import com.hazelcast.spi.AbstractDistributedObject;
import com.hazelcast.spi.NodeEngine;
import com.icodesoftware.service.RecoverService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;
import com.tarantula.game.service.DistributionRoomService;
import com.tarantula.platform.service.cluster.recover.ClusterRecoverService;

public class DistributionRoomServiceProxy extends AbstractDistributedObject<RoomClusterService> implements DistributionRoomService {

    private String objectName;

    public DistributionRoomServiceProxy(String objectName, NodeEngine nodeEngine, RoomClusterService clusterRecoverService){
        super(nodeEngine,clusterRecoverService);
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
    public Stub join(Rating rating) {
        NodeEngine nodeEngine = getNodeEngine();
        int partitionId = nodeEngine.getPartitionService().getPartitionId(rating.xpLevel);
        JoinOperation joinOperation = new JoinOperation();
        nodeEngine.getOperationService().createInvocationBuilder(DistributionRoomService.NAME,joinOperation,partitionId);
        return null;
    }
}
