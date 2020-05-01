package com.tarantula.game;

import com.tarantula.Connection;

public interface RoomListener {
    void onWaiting(Room room);
    void onLeaving(Stub stub);
    void onUpdating(Stub stub);
    Connection onConnection(Room room);
    void onConnecting(Room room);
    void onEnding(Room room);
    byte[] onStarting(Room room);
}
