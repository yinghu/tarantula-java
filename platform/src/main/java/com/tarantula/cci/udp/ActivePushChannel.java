package com.tarantula.cci.udp;

import java.util.concurrent.atomic.AtomicInteger;

public class ActivePushChannel {

    public AtomicInteger totalLeft;


    public ActivePushChannel(){
        totalLeft = new AtomicInteger(0);
    }


    public void reset(int capacity){
        totalLeft.set(capacity);
    }
}
