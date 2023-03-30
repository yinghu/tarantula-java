package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.GameModule;

import java.util.concurrent.atomic.AtomicInteger;

public class ActiveGame{

    public AtomicInteger totalJoined;
    public AtomicInteger totalLeft;

    public GameModule gameModule;


    public ActiveGame(GameModule gameModule){
        this.gameModule = gameModule;
        this.totalJoined = new AtomicInteger(0);
        this.totalLeft = new AtomicInteger(gameModule.room().capacity());
    }

}
