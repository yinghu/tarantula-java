package com.icodesoftware.integration.channel;

import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;

/**
 * Created by yinghu lu on 10/18/2020.
 */
public class RemoteSession {
    public final SocketAddress socketAddress;
    public final FIFOBuffer ackBuffer;
    public RemoteSession(final  SocketAddress socketAddress){
        this.socketAddress = socketAddress;
        this.ackBuffer = new FIFOBuffer(10,new Integer[10]);
    }
}
