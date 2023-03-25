package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.icodesoftware.*;
import com.icodesoftware.protocol.GameModule;
import com.icodesoftware.protocol.UDPEndpointServiceProvider;
import com.tarantula.game.Arena;
import com.tarantula.game.GameZone;
import com.tarantula.game.Rating;

import java.util.List;

public interface GameRoom extends Resettable,Configurable,Portable, UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener,ChannelListener {

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
    boolean dedicated();
    //Local Setup After Join
    void setup(GameZone gameZone,Channel channel,Rating rating);

    //Distributed Methods
    GameRoom join(String systemId,Listener listener);
    GameRoom view();
    void leave(String systemId,Listener listener);
    void load();
    boolean empty();
    boolean full();
    boolean started();

    void setup(Channel channel, GameModule gameModule);


    interface Entry extends Resettable,Configurable, Portable {

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

    interface Listener{
        void onUpdated(GameRoom room,Entry entry);
    }
}
