package com.tarantula.game;

import com.icodesoftware.Connection;

public interface RoomListener {

    Connection onConnecting(Room room);
    void onJoining(Room room);
    PendingUpdate onStarting(Room room);
    PendingUpdate onOverTiming(Room room);
    PendingUpdate onTimeout(Room room);
    PendingUpdate onEnding(Room room);
    PendingUpdate onEnded(Room room);

    void onLeaving(Room room,Stub stub);


}
