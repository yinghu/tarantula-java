package com.tarantula.game;

import com.tarantula.Connection;

public class RoomStateListener implements Connection.StateListener {

    private final Room room;

    public RoomStateListener(Room room){
        this.room = room;
    }

    @Override
    public void onUpdated(byte[] updated) {
        System.out.println(new String(updated));
    }

    @Override
    public void onEnded(byte[] ended) {
        room.end();
        System.out.println(new String(ended));
    }
}
