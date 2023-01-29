package com.icodesoftware.integration.udp;


import java.util.concurrent.atomic.AtomicInteger;

public class ActiveChannel {

    public AtomicInteger totalJoined;
    public AtomicInteger totalLeft;


    public ActiveChannel(int capacity){
        totalJoined = new AtomicInteger(0);
        totalLeft = new AtomicInteger(capacity);
    }
}
