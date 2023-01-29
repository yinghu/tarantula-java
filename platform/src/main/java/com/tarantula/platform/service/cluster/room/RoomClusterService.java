package com.tarantula.platform.service.cluster.room;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.tarantula.platform.room.GameRoom;
import com.tarantula.game.service.GameServiceProvider;
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

    public GameRoom view(String serviceName,String zoneId, String roomId){
        GameServiceProvider gameServiceProvider = (GameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        return gameServiceProvider.roomServiceProvider().onRoomViewed(zoneId,roomId);
    }

    public GameRoom join(String serviceName, String zoneId,String roomId, String systemId){
        GameServiceProvider gameServiceProvider = (GameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        return gameServiceProvider.roomServiceProvider().onRoomJoined(zoneId,roomId,systemId);
    }

    public void leave(String serviceName,String zoneId,String roomId,String systemId){
        GameServiceProvider gameServiceProvider = (GameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        gameServiceProvider.roomServiceProvider().onRoomLeft(zoneId,roomId,systemId);
    }
    public void reset(String serviceName,String zoneId,String roomId){
        GameServiceProvider gameServiceProvider = (GameServiceProvider)this.tarantulaContext.serviceProvider(serviceName);
        gameServiceProvider.roomServiceProvider().onRoomReset(zoneId,roomId);
    }

}
