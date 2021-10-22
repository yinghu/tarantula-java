package com.icodesoftware.protocol;

import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;

public class UserSession {
    public int sessionId;
    public SocketAddress source;
    public FIFOBuffer<MessageHandler> pendingAck;
    public UserSession(int sessionId,SocketAddress source){
        this.sessionId = sessionId;
        this.source = source;
        pendingAck = new FIFOBuffer<>(10,new MessageHandler[10]);
    }
}
