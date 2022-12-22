package com.icodesoftware.protocol;

import java.net.SocketAddress;

public class PendingOutboundMessage {
    public final byte[] payload;
    public final int length;
    public final SocketAddress destination;

    public PendingOutboundMessage(byte[] payload,SocketAddress destination){
        this.payload = payload;
        this.length = payload.length;
        this.destination = destination;
    }
    public PendingOutboundMessage(byte[] buffer,int length,SocketAddress destination){
        this.payload = buffer;
        this.length = length;
        this.destination = destination;
    }
}
