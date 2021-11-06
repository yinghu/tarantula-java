package com.tarantula.game;

import com.icodesoftware.Configurable;
import com.icodesoftware.Initializer;
import com.icodesoftware.Session;
import com.icodesoftware.service.Serviceable;

import java.util.List;

public interface GameLobby extends Configurable, Initializer, Serviceable,GameRoomListener {

    List<GameZone> list();
    Stub join(Session session, Rating rating);
    void leave(Session session);
    void update(Session session, byte[] payload);
    void list(Session session);
    void onTimer();
    String registerTimerListener(TimerListener timerListener);
    void releaseTimerListener(String registerKey);

    boolean configureGameZone(byte[] payload);
    boolean configureArena(byte[] payload);
    void reload();

    interface TimerListener{
        void onTimer();
    }
}