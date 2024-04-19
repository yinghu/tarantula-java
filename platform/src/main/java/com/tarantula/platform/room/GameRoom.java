package com.tarantula.platform.room;

import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.tarantula.game.GameArena;
import com.tarantula.game.Stub;


public interface GameRoom extends Room,DataStore.Updatable,Closable {

    String LABEL = "gameRoom";

    int channelId();
    int sessionId();

    long roomId();

    int bucket();

    void assign(Channel channel);

    boolean empty();
    //Distributed Methods
    GameRoom join(Stub session,RoomStub roomStub);
    GameRoom view();
    void leave(Stub stub);
    void load();
    boolean dedicated();
    long zoneId();

    void setup(Channel[] channels);
    void arena(GameArena arena);
    Channel registerChannel(Session session,Session.TimeoutListener timeoutListener,GameServiceProvider gameServiceProvider);

    interface Entry extends Room.Seat,Resettable {

        String LABEL = "roomEntry";

        void systemId(long systemId);
        void number(int number);
        void stub(long stubId);
        void team(int team);
        void occupied(boolean occupied);
    }
}
