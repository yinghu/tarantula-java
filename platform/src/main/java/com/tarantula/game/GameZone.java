package com.tarantula.game;

import com.icodesoftware.*;
import com.icodesoftware.protocol.MessageBuffer;
import com.tarantula.platform.room.GameRoomRegistry;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public interface GameZone extends Configurable{

    String PLAY_MODE_PVE = "pve"; //player versus computer
    String PLAY_MODE_PVP = "pvp"; //player versus player
    String PLAY_MODE_TVE = "tve"; //team versus computer
    String PLAY_MODE_TVT = "tvt"; //team versus team

    String name();
    void name(String name);
    int levelMatch();
    void levelMatch(int levelMatch);
    String playMode();
    int arenaLimit();
    void arenaLimit(int arenaLimit);
    int capacity();
    void capacity(int capacity);

    int maxJoinsPerRoom();
    void maxJoinsPerRoom(int maxJoinsPerRoom);
    int joinsOnStart();
    void joinsOnStart(int joinsOnStart);
    long roundDuration();
    void roundDuration(long roundDuration);

    boolean connected();
    Stub join(Session session,Rating rating);
    void leave(Stub stub);
    void update(Session session, Stub stub, byte[] payload);
    void list(Session session,Stub stub);
    void addArena(Arena arena);
    List<Arena> arenas();
    Arena arena(int level);
    void setup(ApplicationContext applicationContext,GameLobby gameLobby);
    DataStore dataStore();
    void roomProxy(RoomProxy roomProxy);
    void close();

    ConcurrentHashMap<String,GameRoomRegistry> roomRegistry();
    ConcurrentLinkedDeque<GameRoomRegistry> roomRegistryQueue();

    interface RoomProxy{
        Stub join(Session session,Rating rating);
        void update(Session session, Stub stub, byte[] payload);
        void list(Session session,Stub stub);
        void leave(Stub stub);
        void setup(ApplicationContext applicationContext,GameLobby gameLobby,GameZone gameZone);
        void close();
        byte[] update(Stub stub,MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer);
    }
}
