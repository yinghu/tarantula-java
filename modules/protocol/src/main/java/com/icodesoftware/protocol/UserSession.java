package com.icodesoftware.protocol;

import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UserSession {
    public final int sessionId;
    public SocketAddress source;
    private FIFOBuffer<MessageBuffer.MessageHeader> pendingAckBuffer;
    private AtomicBoolean onJoined;
    private AtomicInteger sequence;
    private int lastSequence;
    //private List<MessageBuffer.MessageHeader> pendingAckList;
    public UserSession(int sessionId,SocketAddress source){
        this.sessionId = sessionId;
        this.source = source;
        pendingAckBuffer = new FIFOBuffer<>(10,new MessageBuffer.MessageHeader[10]);
        onJoined = new AtomicBoolean(true);
        sequence = new AtomicInteger(1);
        lastSequence = 0;
        //pendingAckList = new ArrayList<>(10);
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
    public void pendingAck(MessageBuffer.MessageHeader ack){
        pendingAckBuffer.push(ack);
    }
    public List<MessageBuffer.MessageHeader> pendingAckList(){
        //pendingAckList.clear();
        return pendingAckBuffer.list(new ArrayList<>(10));
    }


}
