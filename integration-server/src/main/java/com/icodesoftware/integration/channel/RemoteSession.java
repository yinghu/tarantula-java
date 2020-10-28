package com.icodesoftware.integration.channel;

import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 10/18/2020.
 */
public class RemoteSession {
    public final SocketAddress socketAddress;
    public final FIFOBuffer ackBuffer;
    public final AtomicInteger pingPong;
    public RemoteSession(final  SocketAddress socketAddress){
        this.socketAddress = socketAddress;
        this.ackBuffer = new FIFOBuffer(20,new Integer[20]);
        this.pingPong = new AtomicInteger(0);
    }
}
