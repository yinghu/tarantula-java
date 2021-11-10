package com.icodesoftware.integration.udp;

import java.util.concurrent.atomic.AtomicInteger;

public class ActiveChannel {

    public byte[] payload;
    public AtomicInteger totalJoined;

    public ActiveChannel(byte[] payload){
        this.payload = payload;
        totalJoined = new AtomicInteger(0);
    }
}
