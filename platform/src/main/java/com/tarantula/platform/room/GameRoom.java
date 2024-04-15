package com.tarantula.platform.room;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.tarantula.game.GameZone;


public interface GameRoom extends Room,Resettable,DataStore.Updatable {

    String LABEL = "gameRoom";

    int channelId();
    int sessionId();

    long roomId();

    int bucket();

    void setup(Channel channel);

    //Distributed Methods
    GameRoom join(Session session);
    GameRoom view();
    void leave(Session session);
    void load();
    boolean dedicated();
    long zoneId();

    void setup(GameServiceProvider gameServiceProvider,GameZone gameZone,boolean dedicated);

    void setup(Channel[] channels);


    Channel registerChannel(Session session,Session.TimeoutListener timeoutListener);

    interface Entry extends Room.Seat,Resettable {

        String LABEL = "roomEntry";

        void systemId(long systemId);
        void number(int number);
        void stub(long stubId);
        void team(int team);
        void occupied(boolean occupied);
    }
}
