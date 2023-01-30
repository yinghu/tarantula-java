package com.icodesoftware.integration.udp;


import java.util.concurrent.atomic.AtomicInteger;

public class ActiveGameChannel {

    public AtomicInteger totalJoined;
    public AtomicInteger totalLeft;


    public ActiveGameChannel(int capacity){
        totalJoined = new AtomicInteger(0);
        totalLeft = new AtomicInteger(capacity);
    }
}
