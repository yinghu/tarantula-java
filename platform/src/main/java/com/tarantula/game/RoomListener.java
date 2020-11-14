package com.tarantula.game;

import com.icodesoftware.Connection;

public interface RoomListener {

    Connection onConnection(Room room);
    void onWaiting(Room room);
    void onJoining(Room room);
    void onInitializing(Room room);
    void onStarting(Room room);
    void onOverTiming(Room room);
    void onEnding(Room room);
    void onEnded(Room room);


    void onLeaving(Stub stub);
    void onUpdating(Stub stub);


    void onTimeout(Room room);

}
