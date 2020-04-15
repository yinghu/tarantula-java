package com.tarantula.game;

import com.tarantula.Connection;

public interface RoomListener {
    void onWaiting(Room room);
    void onLeaving(Stub stub);
    Connection onConnection();
    void onConnecting(Room room);
    void onEnding(Room room);
    byte[] onStarting(Room room);
}
