package com.tarantula.game;

import com.icodesoftware.*;
import com.icodesoftware.Module;

import java.util.List;

public interface GameZone extends Configurable{

    String PLAY_MODE_PVE = "pve"; //player versus computer
    String PLAY_MODE_PVP = "pvp"; //player versus player
    String PLAY_MODE_TVE = "tve"; //team versus computer
    String PLAY_MODE_TVT = "tvt"; //team versus team

    int DEFAULT_LEVEL_COUNT = 10;
    int DEFAULT_JOINS_ON_START = 1;
    long DEFAULT_ROUND_DURATION = 60000;
    int DEFAULT_LEVEL_UP_BASE = 1000;
    int PVE_MAX_ROOM_CAPACITY = 1;
    int PVP_MAX_ROOM_CAPACITY = 2;
    int TVE_MAX_ROOM_CAPACITY = 2;
    int TVT_MAX_ROOM_CAPACITY = 4;

    String name();
    void name(String name);
    String playMode();
    int levelLimit();
    void levelLimit(int levelLimit);
    int capacity();
    void capacity(int capacity);

    int joinsOnStart();
    void joinsOnStart(int capacity);

    long roundDuration();
    void roundDuration(long roundDuration);

    boolean connected();
    Stub join(Session session,Rating rating);
    void leave(String systemId);
    void update(String systemId);
    void onTimer(Module.OnUpdate onUpdate);
    void addArena(Arena arena);
    List<Arena> arenas();
    Arena arena(int level);
    void setup(ApplicationContext applicationContext);

    void roomProxy(RoomProxy roomProxy);

    interface RoomProxy{
        GameRoom join(Session session,Arena  arena,Rating rating);
        void update(String systemId,Tournament.Instance instance);
        void leave(Stub stub);
        default void onTimer(Module.OnUpdate onUpdate){}
        void setup(ApplicationContext applicationContext,GameZone gameZone);
    }
}
