package com.tarantula.game;

import com.icodesoftware.*;
import com.icodesoftware.Module;

import java.util.List;

public interface GameZone extends Configurable,Initializer{

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

    int joinsOnStart();
    void joinsOnStart(int joinsOnStart);

    long roundDuration();
    void roundDuration(long roundDuration);

    boolean connected();
    Stub join(Session session,Rating rating);
    void leave(Stub stub);
    void update(Stub stub);
    void onTimer(Module.OnUpdate onUpdate);
    void addArena(Arena arena);
    List<Arena> arenas();
    Arena arena(int level);

    void roomProxy(RoomProxy roomProxy);

    interface RoomProxy{
        GameRoom join(Session session,String zoneId,Rating rating);
        void update(String systemId,Tournament.Instance instance);
        void leave(Stub stub);
        default void onTimer(Module.OnUpdate onUpdate){}
        void setup(ApplicationContext applicationContext,GameZone gameZone);
    }
}
