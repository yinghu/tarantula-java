package com.tarantula.platform.room;

import com.icodesoftware.Channel;
import com.icodesoftware.Configurable;
import com.icodesoftware.Connection;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;

public interface GameRoom extends Configurable {

    String playMode();
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

    void setup(GameZone gameZone, Rating rating);

    GameRoom join(String systemId);
    GameRoom view();
    void leave(String systemId);

    Channel channel();
    void channel(Channel channel);

    void load();
    String[] joined();

}
