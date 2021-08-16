package com.tarantula.game.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.*;

public interface GameRoomServiceProvider extends ServiceProvider, Configurable.Listener<GameZone> {

    GameRoom join(String zoneId, Rating rating);
    void leave(Arena arena,String roomId,String systemId);
}
