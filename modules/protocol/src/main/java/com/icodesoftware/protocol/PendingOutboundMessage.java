package com.icodesoftware.protocol;

import java.net.SocketAddress;

public class PendingOutboundMessage {
    public final byte[] buffer;
    public final int length;
    public final SocketAddress destination;
    public final boolean buffering;

    public PendingOutboundMessage(byte[] buffer,int length,SocketAddress destination){
        this(buffer,length,destination,true);
    }

    public PendingOutboundMessage(byte[] buffer,int length,SocketAddress destination,boolean buffering){
        this.buffer = buffer;
        this.length = length;
        this.destination = destination;
        this.buffering = buffering;
    }
}
