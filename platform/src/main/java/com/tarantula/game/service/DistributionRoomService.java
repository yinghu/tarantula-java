package com.tarantula.game.service;

import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Rating;
import com.tarantula.game.Stub;

public interface DistributionRoomService extends ServiceProvider {

    String NAME = "RoomService";

    Stub join(String serviceName,Rating rating);
    void leave(String serviceName,String roomId,String systemId);
}
