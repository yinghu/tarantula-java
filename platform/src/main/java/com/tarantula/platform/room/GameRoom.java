package com.tarantula.platform.room;

import com.icodesoftware.Channel;
import com.icodesoftware.Configurable;
import com.tarantula.game.Arena;

public interface GameRoom extends Configurable {

    String roomId();
    int capacity();
    long duration();
    int round();
    Arena arena();

    void setup(Arena arena);

    GameRoom join(String systemId);
    GameRoom view();
    boolean leave(String systemId);

    Channel channel();
    void channel(Channel channel);
}
