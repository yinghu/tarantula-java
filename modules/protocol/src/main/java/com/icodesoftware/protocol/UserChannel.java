package com.icodesoftware.protocol;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserChannel {
    private static TarantulaLogger log = JDKLogger.getLogger(UserChannel.class);
    public final int channelId;
    private ConcurrentHashMap<Integer,UserSession> userSessionIndex;
    private Messenger messenger;
    private UserSessionValidator userSessionValidator;
    private AtomicInteger sequence;
    private ArrayList<Integer> _offline;
    private ConcurrentHashMap<String,PendingAckMessage> pendingAckMessageIndex;
    //private
    public UserChannel(int channelId,Messenger messenger,UserSessionValidator userSessionValidator){
        this.channelId = channelId;
        this.messenger = messenger;
        this.userSessionValidator = userSessionValidator;
        this.userSessionIndex = new ConcurrentHashMap<>();
        this.pendingAckMessageIndex = new ConcurrentHashMap<>();
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
            for(int i=0;i<10;i++){
                String h = messageBuffer.readHeader().toString();
                if(pendingAckMessageIndex.containsKey(h)){
                    PendingAckMessage pendingAckMessage = pendingAckMessageIndex.get(h);
                    pendingAckMessage.pendingAck--;
                    if(pendingAckMessage.pendingAck<=0) pendingAckMessageIndex.remove(h);
                    log.warn("<<<<<<<<<<"+h);
                }
            }
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
        userSession.pendingAck(messageHeader);
        List<MessageBuffer.MessageHeader> _acks = userSession.pendingAckList();
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
        messageBuffer.writeHeader(messageHeader);
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader,messageBuffer);
        userSessionIndex.forEach((sid,session)->{
            messenger.send(messageBuffer,session.source);
            pendingAckMessage.pendingAck++;
        });
        pendingAckMessageIndex.put(messageHeader.toString(),pendingAckMessage);
    }

    private class PendingAckMessage{
        public MessageBuffer.MessageHeader messageHeader;
        public MessageBuffer messageBuffer;
        public int pendingAck;
        public PendingAckMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
            this.messageHeader = messageHeader;
            this.messageBuffer = messageBuffer;
        }
    }

}
