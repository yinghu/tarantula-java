package com.icodesoftware.protocol;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserChannel {

    public final int channelId;
    private ConcurrentHashMap<Integer,UserSession> userSessionIndex;
    private Messenger messenger;
    private UserSessionValidator userSessionValidator;
    private AtomicInteger sequence;
    private ArrayList<Integer> _offline;
    //private
    public UserChannel(int channelId,Messenger messenger,UserSessionValidator userSessionValidator){
        this.channelId = channelId;
        this.messenger = messenger;
        this.userSessionValidator = userSessionValidator;
        this.userSessionIndex = new ConcurrentHashMap<>();
        this.sequence = new AtomicInteger(0);
        this._offline = new ArrayList<>();
    }

    public void onMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer, SocketAddress source){
        UserSession userSession = userSessionIndex.computeIfAbsent(messageHeader.sessionId,k-> {
            if(messageHeader.commandId != Messenger.JOIN) return null;
            return userSessionValidator.validate(messageHeader, messageBuffer) ? new UserSession(k, source) : null;
        });
        if(userSession==null){
            return;
        }
        if(userSession.onJoin()){
            //server push onJoin 200
            onJoin(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.commandId == Messenger.ACK){
            //server clear on ack
            return;
        }
        if(messageHeader.commandId == Messenger.PING){
            //update user session
            userSession.ping();
            return;
        }
        userSessionIndex.forEach((sid,session)->{
            if(messageHeader.sessionId!=sid) messenger.send(messageBuffer,session.source);
        });
        if(!messageHeader.ack) return;
        onAck(userSession,messageHeader,messageBuffer,source);
    }
    public void onTimer(){
        userSessionIndex.forEach((k,v)->{
            if(!v.online()) _offline.add(k);
        });
        _offline.forEach((i)-> userSessionIndex.remove(i));
        _offline.clear();
    }
    private void onAck(UserSession userSession, MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer,SocketAddress source){
        userSession.pendingAck.push(messageHeader);
        List<MessageBuffer.MessageHeader> _acks = userSession.pendingAck.list(new ArrayList<>());
        messageBuffer.reset();
        MessageBuffer.MessageHeader ackHeader = new MessageBuffer.MessageHeader();
        ackHeader.commandId = Messenger.ACK;
        messageBuffer.writeHeader(ackHeader);
        _acks.forEach((mh)->messageBuffer.writeHeader(mh));
        messenger.send(messageBuffer,source);
    }
    private void onJoin(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        messageBuffer.reset();
        messageHeader.ack = true;
        messageHeader.commandId = Messenger.ON_JOIN;
        messageHeader.sequence = sequence.incrementAndGet();
        messageBuffer.writeHeader(messageHeader);
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader,messageBuffer);
        userSessionIndex.forEach((sid,session)->messenger.send(messageBuffer,session.source));
    }

    private class PendingAckMessage{
        public MessageBuffer.MessageHeader messageHeader;
        public MessageBuffer messageBuffer;
        public PendingAckMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
            this.messageHeader = messageHeader;
            this.messageBuffer = messageBuffer;
        }
    }

}
