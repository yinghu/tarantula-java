package com.tarantula.platform.room;

import com.icodesoftware.Channel;
import com.icodesoftware.Configurable;
import com.icodesoftware.Connection;
import com.tarantula.game.Arena;

public interface GameRoom extends Configurable {

    int channelId();
    int sessionId();
    int timeout();
    byte[] serverKey();
    Connection connection();

    String roomId();
    int capacity();
    long duration();
    int round();
    Arena arena();

    void setup(Arena arena);

    GameRoom join(String systemId,RoomListener roomListener);
    GameRoom view();
    void leave(String systemId,RoomListener roomListener);
    boolean resetIfEmpty();

    Channel channel();
    void channel(Channel channel);

    interface RoomListener{
        boolean onRoom(GameRoom gameRoom);
    }

    interface RoomRegistryListener{
        boolean onRegistry(GameRoomRegistry gameRoomRegistry);
    }
}
