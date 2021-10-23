package com.icodesoftware.protocol;

import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UserSession {
    public final int sessionId;
    public SocketAddress source;
    public FIFOBuffer<MessageBuffer.MessageHeader> pendingAck;
    private AtomicBoolean onJoined;
    private AtomicInteger sequence;
    private int lastSequence;
    public UserSession(int sessionId,SocketAddress source){
        this.sessionId = sessionId;
        this.source = source;
        pendingAck = new FIFOBuffer<>(10,new MessageBuffer.MessageHeader[10]);
        onJoined = new AtomicBoolean(true);
        sequence = new AtomicInteger(1);
        lastSequence = 0;
    }
    public boolean onJoin(){
        return onJoined.getAndSet(false);
    }
    public void ping(){
        sequence.incrementAndGet();
    }
    public boolean online(){
        int seq = sequence.get();
        if(lastSequence == seq) return false;
        lastSequence = seq;
        return true;
    }


}
