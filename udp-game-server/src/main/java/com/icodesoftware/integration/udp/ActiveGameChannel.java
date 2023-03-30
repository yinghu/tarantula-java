package com.icodesoftware.integration.udp;

import com.icodesoftware.protocol.ChannelHeader;


import java.util.concurrent.atomic.AtomicInteger;

public class ActiveGameChannel extends ChannelHeader {

    public AtomicInteger totalJoined;
    public AtomicInteger totalLeft;


    public ActiveGameChannel(int capacity){
        totalJoined = new AtomicInteger(0);
        totalLeft = new AtomicInteger(capacity);
    }

}
