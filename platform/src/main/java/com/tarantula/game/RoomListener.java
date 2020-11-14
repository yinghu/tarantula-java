package com.tarantula.game;

import com.icodesoftware.Connection;

public interface RoomListener {

    Connection onConnecting(Room room);
    void onJoining(Room room);
    void onInitializing(Room room);
    void onStarting(Room room);
    void onOverTiming(Room room);
    void onTimeout(Room room);
    void onEnding(Room room);
    void onEnded(Room room);

    void onLeaving(Room room,Stub stub);


}
