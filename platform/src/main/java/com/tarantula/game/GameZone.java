package com.tarantula.game;

import com.icodesoftware.*;

import java.util.List;

public interface GameZone extends Configurable{

    String PLAY_MODE_PVE = "pve"; //player versus computer
    String PLAY_MODE_PVP = "pvp"; //player versus player
    String PLAY_MODE_TVE = "tve"; //team versus computer
    String PLAY_MODE_TVT = "tvt"; //team versus team

    String name();
    void name(String name);

    String playMode();

    String gameModule();

    int capacity();
    int joinsOnStart();
    long roundDuration();
    long roundOvertime();

    boolean connected();
    Stub join(Session session);
    boolean leave(Stub stub);

    List<GameArena> arenas();
    GameArena arena(int level);
    void setup(ApplicationContext applicationContext,GameLobby gameLobby);
    DataStore dataStore();
    void roomProxy(RoomProxy roomProxy);
    void close();

    interface RoomProxy{
        Stub join(Session session);
        boolean leave(Stub stub);
        void setup(ApplicationContext applicationContext,GameLobby gameLobby,GameZone gameZone);
    }
}
