package com.tarantula.game.service;

import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.Rating;

public interface DistributionRoomService extends ServiceProvider {

    String NAME = "DistributionRoomService";
    String register(String serviceName,String zoneId,Rating rating);
    GameRoom join(String serviceName,Arena arena,String roomId, String systemId);
    void leave(String serviceName,Arena arena, String roomId,String systemId);
}
