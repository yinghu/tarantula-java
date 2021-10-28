package com.tarantula.game;

import com.icodesoftware.*;
import com.icodesoftware.Module;
import com.tarantula.platform.IndexSet;
import com.tarantula.platform.room.GameRoom;
import com.tarantula.platform.room.GameRoomRegistry;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
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
    void update(Session session, Stub stub, byte[] payload, Module.OnUpdate onUpdate);
    void list(Session session,Stub stub);
    void addArena(Arena arena);
    List<Arena> arenas();
    Arena arena(int level);
    void setup(ApplicationContext applicationContext,GameLobby gameLobby);
    DataStore dataStore();
    void roomProxy(RoomProxy roomProxy);
    void close();

    ConcurrentHashMap<String,GameRoomRegistry> roomRegistry();
    ArrayBlockingQueue<GameRoomRegistry> roomRegistryQueue();

    interface RoomProxy{
        Stub join(Session session,Rating rating);
        void update(Session session, Stub stub, byte[] payload, Module.OnUpdate onUpdate);
        void list(Session session,Stub stub);
        void leave(Stub stub);
        void setup(ApplicationContext applicationContext,GameLobby gameLobby,GameZone gameZone);
        default void close(){}
        default String onRegister(Rating rating){ return null;}
        default GameRoom onJoin(Arena arena, String roomId, String systemId){return null;}
        default void onLeave(String roomId,String systemId){}
    }
}
