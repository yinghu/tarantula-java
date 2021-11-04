package com.icodesoftware.protocol;

import java.net.SocketAddress;

public class PendingOutboundMessage {
    public final byte[] payload;
    public final SocketAddress destination;

    public PendingOutboundMessage(byte[] payload,SocketAddress destination){
        this.payload = payload;
        this.destination = destination;
    }
}
