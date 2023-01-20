package com.tarantula.platform.room;

import com.icodesoftware.Channel;
import com.icodesoftware.Configurable;
import com.icodesoftware.Connection;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;

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

    //Local Setup After Join
    void setup(GameZone gameZone,Channel channel,Rating rating);

    //Distributed Methods
    GameRoom join(String systemId);
    GameRoom view();
    void leave(String systemId);
    void load();

}
