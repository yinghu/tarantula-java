package com.icodesoftware.protocol;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserChannel {
    //private static TarantulaLogger log = JDKLogger.getLogger(UserChannel.class);
    public final int channelId;
    private ConcurrentHashMap<Integer,UserSession> userSessionIndex;
    private Messenger messenger;
    private UDPEndpointServiceProvider.UserSessionValidator userSessionValidator;
    private UDPEndpointServiceProvider.RequestListener requestListener;
    private AtomicInteger sequence;
    private ArrayList<Integer> _offline;
    private ArrayList<String> _retried;
    private ConcurrentHashMap<String,PendingAckMessage> pendingAckMessageIndex;
    private UDPEndpointServiceProvider.SessionListener sessionListener;

    public UserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener, UDPEndpointServiceProvider.RequestListener requestListener){
        this.channelId = channelId;
        this.messenger = messenger;
        this.userSessionValidator = userSessionValidator;
        this.requestListener = requestListener;
        this.userSessionIndex = new ConcurrentHashMap<>();
        this.pendingAckMessageIndex = new ConcurrentHashMap<>();
        this.sequence = new AtomicInteger(0);
        this._offline = new ArrayList<>();
        this._retried = new ArrayList<>();
        this.sessionListener = sessionListener;
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
                }
            }
            return;
        }
        if(messageHeader.commandId == Messenger.PING){
            //update user session
            userSession.ping();
            return;
        }
        if(messageHeader.commandId == Messenger.REQUEST){
            requestListener.onMessage(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.channelId == Messenger.LEAVE){
            onLeave(messageHeader,messageBuffer);
            return;
        }
        messageBuffer.rewind();
        byte[] payload = messageBuffer.toArray();
        userSessionIndex.forEach((sid,session)->{
            if(!messageHeader.broadcasting){
                if(messageHeader.sessionId!=sid) messenger.send(payload,session.source);
            }
            else{
                messenger.send(payload,session.source);
            }
        });
        if(!messageHeader.ack) return;
        onAck(userSession,messageHeader,messageBuffer,source);
    }
    public void onKickoff(){
        userSessionIndex.forEach((k,v)->{
            if(!v.online()) _offline.add(k);
        });
        _offline.forEach((i)-> {
            userSessionIndex.remove(i);
            if(sessionListener!=null) sessionListener.onTimeout(channelId,i);
        });
        _offline.clear();
    }
    public void onRetry(){
        _retried.clear();
        pendingAckMessageIndex.forEach((k,v)-> {
           userSessionIndex.forEach((uk, uu) -> {
               messenger.send(v.data, uu.source);
           });
           v.retries--;
           if(v.retries<=0){
               _retried.add(k);
           }
        });
        _retried.forEach(k->pendingAckMessageIndex.remove(k));
    }
    public void queue(int sessionId,byte[] data){
        messenger.queue(data,userSessionIndex.get(sessionId).source);
    }
    private void onAck(UserSession userSession, MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer,SocketAddress source){
        userSession.pendingAck(messageHeader);
        List<MessageBuffer.MessageHeader> _acks = userSession.pendingAckList();
        messageBuffer.reset();
        MessageBuffer.MessageHeader ackHeader = new MessageBuffer.MessageHeader();
        ackHeader.commandId = Messenger.ACK;
        messageBuffer.writeHeader(ackHeader);
        _acks.forEach((mh)->messageBuffer.writeHeader(mh));//need to sync
        messageBuffer.flip();
        byte[] payload = messageBuffer.toArray();
        messenger.send(payload,source);
    }
    private void onJoin(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        messageBuffer.reset();
        messageHeader.ack = true;
        messageHeader.commandId = Messenger.ON_JOIN;
        messageHeader.sequence = sequence.incrementAndGet();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.flip();
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader,messageBuffer.toArray());
        userSessionIndex.forEach((sid,session)->{
            messenger.send(pendingAckMessage.data,session.source);
            pendingAckMessage.pendingAck++;
        });
        pendingAckMessageIndex.put(messageHeader.toString(),pendingAckMessage);
    }
    private void onLeave(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        userSessionIndex.remove(messageHeader.sessionId);
    }

    private class PendingAckMessage{
        public MessageBuffer.MessageHeader messageHeader;
        public byte[] data;
        public int retries = 3;
        public int pendingAck;
        public PendingAckMessage(MessageBuffer.MessageHeader messageHeader,byte[] data){
            this.messageHeader = messageHeader;
            this.data = data;
        }
    }

}
