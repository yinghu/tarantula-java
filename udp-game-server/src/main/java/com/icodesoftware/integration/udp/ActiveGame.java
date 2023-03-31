package com.icodesoftware.integration.udp;

import com.icodesoftware.Room;
import com.icodesoftware.RoomListener;
import com.icodesoftware.protocol.GameModule;

import java.util.concurrent.atomic.AtomicInteger;

public class ActiveGame implements RoomListener {

    public AtomicInteger totalJoined;
    public AtomicInteger totalLeft;

    public GameModule gameModule;

    public ActiveGame(GameModule gameModule){
        this.gameModule = gameModule;
        this.totalJoined = new AtomicInteger(0);
        this.totalLeft = new AtomicInteger(gameModule.room().capacity());
    }

    @Override
    public void onStarted(Room room) {
        System.out.println("room started");
    }

    @Override
    public void onEnded(Room room) {
        System.out.println("room ended");
    }
}
