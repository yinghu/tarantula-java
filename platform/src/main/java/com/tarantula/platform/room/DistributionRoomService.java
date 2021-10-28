package com.tarantula.platform.room;

import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Rating;

public interface DistributionRoomService extends ServiceProvider {

    String NAME = "DistributionRoomService";
    RoomJoinStub register(String serviceName,String zoneId,Rating rating);

    void release(String serviceName,String zoneId,String roomId);
    GameRoom view(String serviceName,String roomId);
    GameRoom join(String serviceName,String ticket,String roomId, String systemId);
    void leave(String serviceName,String roomId,String systemId);
    boolean localManaged(String key);
    void create(String serviceName,String zoneId,String roomId);
    void load(String serviceName,String roomId);
}
