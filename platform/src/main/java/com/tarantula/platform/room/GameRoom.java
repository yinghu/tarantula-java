package com.tarantula.platform.room;

import com.hazelcast.nio.serialization.Portable;
import com.icodesoftware.*;
import com.icodesoftware.protocol.*;
import com.tarantula.game.GameZone;

import java.util.List;

public interface GameRoom extends Room,Resettable,Closable,Configurable,Portable, UDPEndpointServiceProvider.RequestListener,UDPEndpointServiceProvider.ActionListener, ChannelListener {

    String LABEL = "gameRoom";

    int channelId();
    int sessionId();

    byte[] serverKey();
    Connection connection();

    String roomId();

    List<Entry> entries();
    void setup(Channel channel);

    //Distributed Methods
    GameRoom join(long stubId,Listener listener);
    GameRoom view();
    void leave(long stubId,Listener listener);
    void load();


    void setup(GameZone gameZone,GameModule gameModule,boolean dedicated);

    void setup(Channel[] channels);

    void onUpdated(GameServiceProvider gameServiceProvider,byte[] payload);


    Channel registerChannel(Session session,Session.TimeoutListener timeoutListener);

    interface Entry extends Resettable,Configurable, Portable {

        String LABEL = "roomEntry";

        int seat();
        long stubId();
        int team();
        boolean occupied();

        void seat(int seat);
        void stubId(long stubId);
        void team(int team);
        void occupied(boolean occupied);
    }

    interface Listener{
        void onUpdated(GameRoom room,Entry entry);
    }

    static GameRoom newGameRoom(String type,int roomCapacity){
        GameRoom gameRoom = null;
        switch (type){
            case GameZone.PLAY_MODE_PVE:
                gameRoom = new PVEGameRoom();
                break;
            case GameZone.PLAY_MODE_PVP:
                gameRoom = new PVPGameRoom(roomCapacity);
                break;
            case GameZone.PLAY_MODE_TVE:
                gameRoom = new TVEGameRoom(roomCapacity);
                break;
            case GameZone.PLAY_MODE_TVT:
                gameRoom = new TVTGameRoom(roomCapacity);
                break;
        }
        return gameRoom;
    }
}
