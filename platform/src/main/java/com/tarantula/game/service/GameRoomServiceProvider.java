package com.tarantula.game.service;

import com.icodesoftware.Configurable;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.game.Arena;
import com.tarantula.game.GameRoom;
import com.tarantula.game.Rating;

public interface GameRoomServiceProvider extends ServiceProvider, Configurable.Listener {

    GameRoom join(Arena arena, Rating rating);
}
