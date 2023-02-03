package com.tarantula.game;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageBuffer;

import java.util.List;

public interface GameZone extends Configurable{

    String PLAY_MODE_PVE = "pve"; //player versus computer
    String PLAY_MODE_PVP = "pvp"; //player versus player
    String PLAY_MODE_TVE = "tve"; //team versus computer
    String PLAY_MODE_TVT = "tvt"; //team versus team

    String name();
    void name(String name);

    String playMode();
    int capacity();
    int joinsOnStart();
    long roundDuration();

    boolean connected();
    Stub join(Session session,Rating rating);
    boolean leave(Stub stub);
    byte[] onService(Stub stub, byte[] payload);
    //void list(Session session,Stub stub);

    List<Arena> arenas();
    Arena arena(int level);
    void setup(ApplicationContext applicationContext,GameLobby gameLobby);
    DataStore dataStore();
    void roomProxy(RoomProxy roomProxy);
    void close();

    interface RoomProxy{
        Stub join(Session session,Rating rating);

        //void list(Session session,Stub stub);
        boolean leave(Stub stub);
        void setup(ApplicationContext applicationContext,GameLobby gameLobby,GameZone gameZone);
        void close();

        byte[] onService(Stub stub, byte[] payload);
        byte[] onService(Stub stub,MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }
}
