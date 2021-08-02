package com.tarantula.platform.service.cluster.room;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.game.Rating;
import com.tarantula.game.Room;
import com.tarantula.game.service.GameServiceProvider;
import com.tarantula.game.service.RoomServiceProvider;
import com.tarantula.platform.TarantulaContext;

import java.util.Properties;

public class RoomClusterService implements ManagedService, RemoteService {

    private static TarantulaLogger log = JDKLogger.getLogger(RoomClusterService.class);

    private NodeEngine nodeEngine;
    private TarantulaContext tarantulaContext;

    @Override
    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.tarantulaContext = TarantulaContext.getInstance();
        log.warn("Start Room cluster service");
    }

    @Override
    public void reset() {

    }

    @Override
    public void shutdown(boolean b) {

    }

    @Override
    public DistributedObject createDistributedObject(String objectName) {
        return new DistributionRoomServiceProxy(objectName,nodeEngine,this);
    }

    @Override
    public void destroyDistributedObject(String s) {

    }

    public Room join(String serviceName, Rating rating){
        GameServiceProvider gameServiceProvider = (GameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        RoomServiceProvider roomServiceProvider = gameServiceProvider.roomServiceProvider();
        return roomServiceProvider.onJoin(rating);
    }

    public void leave(String serviceName,String roomId,String systemId){
        GameServiceProvider gameServiceProvider = (GameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        RoomServiceProvider roomServiceProvider = gameServiceProvider.roomServiceProvider();
        roomServiceProvider.onLeave(roomId,systemId);
    }
}
