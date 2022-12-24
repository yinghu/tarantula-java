package com.icodesoftware.protocol;

import java.net.SocketAddress;

public class PendingOutboundMessage {
    public final byte[] buffer;
    public final int length;
    public final SocketAddress destination;

    public PendingOutboundMessage(byte[] payload,SocketAddress destination){
        this.buffer = payload;
        this.length = payload.length;
        this.destination = destination;
    }
    public PendingOutboundMessage(byte[] buffer,int length,SocketAddress destination){
        this.buffer = buffer;
        this.length = length;
        this.destination = destination;
    }
}
