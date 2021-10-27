package com.tarantula.game;

import com.tarantula.platform.room.GameRoom;

public interface GameRoomListener {
    void timeout(GameRoom room);
}
