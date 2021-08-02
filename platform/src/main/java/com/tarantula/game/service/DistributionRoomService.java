package com.tarantula.game.service;

import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Arena;
import com.tarantula.game.Rating;
import com.tarantula.game.Room;

public interface DistributionRoomService extends ServiceProvider {

    String NAME = "DistributionRoomService";

    Room join(String serviceName, Arena arena,Rating rating);
    void leave(String serviceName,Arena arena, String roomId,String systemId);
}
