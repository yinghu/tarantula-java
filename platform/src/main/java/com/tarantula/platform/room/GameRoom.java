package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.icodesoftware.Channel;
import com.icodesoftware.Configurable;
import com.icodesoftware.Connection;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;

import java.util.List;

public interface GameRoom extends Configurable {

    String LABEL = "ZGR";

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
    List<Entry> entries();

    //Local Setup After Join
    void setup(GameZone gameZone,Channel channel,Rating rating);

    //Distributed Methods
    GameRoom join(String systemId);
    GameRoom view();
    void leave(String systemId);
    void load();

    interface Entry extends Configurable, Portable {

        String LABEL = "GGE";

        int seat();
        String systemId();
        int team();
        boolean occupied();

        void seat(int seat);
        void systemId(String systemId);
        void team(int team);
        void occupied(boolean occupied);
    }
}
