package com.tarantula.game;

import com.icodesoftware.Configurable;
import com.icodesoftware.Initializer;
import com.icodesoftware.Module;
import com.icodesoftware.Session;
import com.icodesoftware.service.Serviceable;

import java.util.List;

public interface GameLobby extends Configurable, Initializer, Serviceable {

    List<GameZone> list();
    Stub join(Session session, Rating rating);
    void leave(Session session);
    void update(Session session, byte[] payload, Module.OnUpdate onUpdate);
    void onTimer(Module.OnUpdate onUpdate);
    String registerTimerListener(TimerLister timerLister);
    void releaseTimerListener(String registerKey);

    boolean configureGameZone(byte[] payload);
    boolean configureArena(byte[] payload);
    void reload();

    interface TimerLister{
        void onTimer(Module.OnUpdate onUpdate);
    }
}