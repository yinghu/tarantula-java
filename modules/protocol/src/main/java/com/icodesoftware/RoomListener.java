package com.icodesoftware;

public interface RoomListener {

    void onStarted(Room room);
    void onUpdated(Room room,byte[] payload);
    void onEnded(Room room);
}
