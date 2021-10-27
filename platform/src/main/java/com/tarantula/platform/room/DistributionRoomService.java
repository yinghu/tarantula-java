package com.tarantula.platform.room;

import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.GameRoom;
import com.tarantula.game.Rating;

public interface DistributionRoomService extends ServiceProvider {

    String NAME = "DistributionRoomService";
    GameRoomRegistry register(String serviceName,String zoneId,Rating rating);
    void release(String serviceName,String zoneId,String roomId);
    GameRoom join(String serviceName,String roomId, String systemId);
    void leave(String serviceName,String roomId,String systemId);
    boolean localManaged(String key);
}
