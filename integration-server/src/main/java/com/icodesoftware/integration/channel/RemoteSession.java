package com.icodesoftware.integration.channel;

import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yinghu lu on 10/18/2020.
 */
public class RemoteSession {
    public final SocketAddress socketAddress;
    public final FIFOBuffer ackBuffer;
    public final AtomicLong lastPong;
    public RemoteSession(final  SocketAddress socketAddress,long timestamp){
        this.socketAddress = socketAddress;
        this.ackBuffer = new FIFOBuffer(20,new Integer[20]);
        this.lastPong = new AtomicLong(timestamp);
    }
}
