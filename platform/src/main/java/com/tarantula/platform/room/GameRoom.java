package com.tarantula.platform.room;

import com.icodesoftware.Channel;
import com.icodesoftware.Configurable;
import com.icodesoftware.Connection;
import com.tarantula.game.Arena;

public interface GameRoom extends Configurable {

    int channelId();
    int sessionId();
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
    boolean leave(String systemId);

    Channel channel();
    void channel(Channel channel);

    interface RoomListener{
        boolean onJoin(GameRoom gameRoom);
    }
}
