package com.tarantula.cci.udp;

import com.icodesoftware.Channel;

import java.util.concurrent.atomic.AtomicLong;

public class PendingJoinChannel {
    public final Channel channel;
    public final AtomicLong timeout;

    public PendingJoinChannel(Channel channel,long timeout){
        this.channel = channel;
        this.timeout = new AtomicLong(timeout);
    }

}
