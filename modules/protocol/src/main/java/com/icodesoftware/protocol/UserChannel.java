package com.icodesoftware.protocol;

import com.icodesoftware.util.TimeUtil;

import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class UserChannel {

    protected int channelId;
    protected ConcurrentHashMap<Integer,UserSession> userSessionIndex;
    protected Messenger messenger;
    protected UDPEndpointServiceProvider.UserSessionValidator userSessionValidator;
    protected UDPEndpointServiceProvider.RequestListener requestListener;
    protected AtomicInteger sequence;
    protected ArrayList<Integer> _offline;
    protected ArrayList<String> _retried;
    protected ConcurrentHashMap<String,PendingAckMessage> pendingAckMessageIndex;
    protected ConcurrentLinkedDeque<PendingActionMessage> pendingActionMessageQueue;
    protected UDPEndpointServiceProvider.SessionListener sessionListener;

    private ArrayList<PendingActionMessage> requeueList;
    protected MessageBuffer.MessageHeader pingHeader;
    protected MessageBuffer pingBuffer;

    public UserChannel(int channelId, Messenger messenger, UDPEndpointServiceProvider.UserSessionValidator userSessionValidator, UDPEndpointServiceProvider.SessionListener sessionListener, UDPEndpointServiceProvider.RequestListener requestListener){
        this.channelId = channelId;
        this.messenger = messenger;
        this.userSessionValidator = userSessionValidator!=null?userSessionValidator:(h,m)->false;
        this.requestListener = requestListener!=null?requestListener:(h,m)->null;
        this.userSessionIndex = new ConcurrentHashMap<>();
        this.pendingAckMessageIndex = new ConcurrentHashMap<>();
        this.pendingActionMessageQueue = new ConcurrentLinkedDeque<>();
        this.sequence = new AtomicInteger(0);
        this._offline = new ArrayList<>();
        this._retried = new ArrayList<>();
        this.sessionListener = sessionListener!=null?sessionListener:(c,s)->{};
        this.requeueList = new ArrayList<>();
        this.pingHeader = new MessageBuffer.MessageHeader();
        this.pingHeader.commandId = Messenger.PING;
        this.pingHeader.channelId = channelId;
        this.pingBuffer = new MessageBuffer();
    }
    public int channelId(){
        return this.channelId;
    }
    public void channelId(int channelId){
        this.channelId = channelId;
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
        if(messageHeader.commandId == Messenger.JOIN){
            if(!userSessionValidator.validate(messageHeader,messageBuffer)){
                //kickoff
                userSessionIndex.remove(messageHeader.sessionId);
                sessionListener.onTimeout(channelId,messageHeader.sessionId);
                return;
            }
            userSession.source = source;
            userSession.onPing();
            onJoin(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.commandId == Messenger.ACK){
            //server clear on ack
            for(int i=0;i<MessageBuffer.PENDING_ACK_SIZE;i++){
                String h = messageBuffer.readHeader().toString();
                if(pendingAckMessageIndex.containsKey(h)){
                    PendingAckMessage pendingAckMessage = pendingAckMessageIndex.get(h);
                    pendingAckMessage.pendingAck--;
                    if(pendingAckMessage.pendingAck<=0){
                        PendingAckMessage removed = pendingAckMessageIndex.remove(h);
                        if(removed!=null) messenger.buffer(removed.buffer);
                    }
                }
            }
            return;
        }
        if(messageHeader.commandId == Messenger.PING){
            //update user session
            userSession.onPing();
            return;
        }
        if(messageHeader.commandId == Messenger.REQUEST){
            requestListener.onMessage(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.commandId == Messenger.LEAVE){
            onLeave(messageHeader,messageBuffer);
            return;
        }
        messageBuffer.rewind();
        onRelay(messageHeader,messageBuffer);
        int pendingTime = messageHeader.batchSize*Short.MAX_VALUE+messageHeader.batch;
        if(pendingTime>0){
            MessageBuffer.MessageHeader pendingHeader = messageHeader.copy();
            pendingHeader.commandId += Messenger.ON_PENDING_ACTION;
            messageBuffer.rewind();
            messageBuffer.readHeader();
            byte[] data = messageBuffer.readPayload();
            messageBuffer.reset();
            messageBuffer.writeHeader(pendingHeader);
            messageBuffer.writePayload(data);
            messageBuffer.rewind();
            byte[] buffer = messenger.buffer();
            int length = messageBuffer.toArray(buffer);
            pendingActionMessageQueue.offer(new PendingActionMessage(buffer,length,pendingTime,messageHeader));
        }
        if(!messageHeader.ack) return;
        onAck(userSession,messageHeader,messageBuffer,source);
    }
    public void onKickoff(){
        userSessionIndex.forEach((k,v)->{
            if(!v.online()) _offline.add(k);
        });
        _offline.forEach((i)-> {
            userSessionIndex.remove(i);
            sessionListener.onTimeout(channelId,i);
        });
        _offline.clear();
    }
    public void onRetry(){
        _retried.clear();
        pendingAckMessageIndex.forEach((k,v)-> {
           userSessionIndex.forEach((uk, uu) -> {
               messenger.queue(v.buffer,v.length,uu.source);
           });
           v.retries--;
           if(v.retries<=0){
               _retried.add(k);
           }
        });
        _retried.forEach(k->{
            PendingAckMessage removed = pendingAckMessageIndex.remove(k);
            if(removed!=null) messenger.buffer(removed.buffer);
        });
    }
    protected void onPing(){
        userSessionIndex.forEach((k,v)->{
            pingBuffer.reset();
            pingHeader.sessionId = k;
            pingBuffer.writeHeader(pingHeader);
            pingBuffer.writeLong(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
            pingBuffer.flip();
            messenger.queue(pingBuffer,v.source);
        });
    }
    public void queue(int sessionId,MessageBuffer messageBuffer){
        messenger.queue(messageBuffer,userSessionIndex.get(sessionId).source);
    }
    //public void send(int sessionId,MessageBuffer messageBuffer){
        //messenger.send(messageBuffer,userSessionIndex.get(sessionId).source);
    //}
    public void kickoff(int sessionId){
        userSessionIndex.remove(sessionId);
        sessionListener.onTimeout(channelId,sessionId);
    }
    public void onPendingAction(int frameRate){
        requeueList.clear();
        PendingActionMessage p;
        do{
            p = pendingActionMessageQueue.poll();
            if(p!=null ){
                p.pendingTime -= frameRate;
                if(p.pendingTime>0) {
                    requeueList.add(p);
                }
                else{
                    byte[] data = p.buffer;
                    int[] pendingAck ={0};
                    userSessionIndex.forEach((k,v)->{
                        messenger.queue(data,data.length,v.source);
                        pendingAck[0]++;
                    });
                    if(p.messageHeader.ack && pendingAck[0]>0){
                        PendingAckMessage pendingAckMessage = new PendingAckMessage(p.messageHeader,data,p.length);
                        pendingAckMessage.pendingAck = pendingAck[0];
                        pendingAckMessageIndex.put(p.messageHeader.toString(),pendingAckMessage);
                    }
                }
            }
        }while (p != null);
        requeueList.forEach(pr->pendingActionMessageQueue.offer(pr));
    }
    private void onAck(UserSession userSession, MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer,SocketAddress source){
        userSession.pendingAck(messageHeader);
        List<MessageBuffer.MessageHeader> _acks = userSession.pendingAckList();
        messageBuffer.reset();
        MessageBuffer.MessageHeader ackHeader = new MessageBuffer.MessageHeader();
        ackHeader.commandId = Messenger.ACK;
        messageBuffer.writeHeader(ackHeader);
        _acks.forEach((mh)->messageBuffer.writeHeader(mh));
        messageBuffer.flip();
        messenger.queue(messageBuffer,source);
    }
    protected void onJoin(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        messageBuffer.reset();
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.commandId = Messenger.ON_JOIN;
        messageHeader.sequence = sequence.incrementAndGet();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(messageHeader.sessionId);
        messageBuffer.writeLong(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        messageBuffer.flip();
        byte[] buffer = messenger.buffer();
        int length = messageBuffer.toArray(buffer);
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader,buffer,length);
        userSessionIndex.forEach((sid,session)->{
            messenger.queue(pendingAckMessage.buffer,pendingAckMessage.length,session.source);
            pendingAckMessage.pendingAck++;
        });
        pendingAckMessageIndex.put(messageHeader.toString(),pendingAckMessage);
    }
    protected void onLeave(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        userSessionIndex.remove(messageHeader.sessionId);
        if(sessionListener!=null) sessionListener.onTimeout(channelId,messageHeader.sessionId);
        messageBuffer.reset();
        messageHeader.ack = true;
        messageHeader.encrypted = false;
        messageHeader.commandId = Messenger.ON_LEAVE;
        messageHeader.sequence = sequence.incrementAndGet();
        messageBuffer.writeHeader(messageHeader);
        messageBuffer.writeInt(messageHeader.sessionId);
        messageBuffer.flip();
        byte[] buffer = messenger.buffer();
        int length = messageBuffer.toArray(buffer);
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader,buffer,length);
        userSessionIndex.forEach((sid,session)->{
            messenger.queue(pendingAckMessage.buffer,pendingAckMessage.length,session.source);
            pendingAckMessage.pendingAck++;
        });
        pendingAckMessageIndex.put(messageHeader.toString(),pendingAckMessage);
    }
    protected void onRelay(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        byte[] buffer = messenger.buffer();
        int length = messageBuffer.toArray(buffer);
        int[] pendingAck ={0};
        userSessionIndex.forEach((sid,session)->{
            if(!messageHeader.broadcasting){
                if(messageHeader.sessionId!=sid){
                    messenger.queue(buffer,length,session.source);
                    pendingAck[0]++;
                }
            }
            else{
                messenger.queue(buffer,length,session.source);
                pendingAck[0]++;
            }
        });
        if(!messageHeader.ack||pendingAck[0]==0) return;
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader,buffer,length);
        pendingAckMessage.pendingAck = pendingAck[0];
        pendingAckMessageIndex.put(messageHeader.toString(),pendingAckMessage);
    }
    protected class PendingAckMessage{
        public MessageBuffer.MessageHeader messageHeader;
        public byte[] buffer;
        public int length;
        public int retries = MessageBuffer.RETRIES;
        public int pendingAck;
        public PendingAckMessage(MessageBuffer.MessageHeader messageHeader,byte[] buffer,int length){
            this.messageHeader = messageHeader;
            this.buffer = buffer;
            this.length = length;
        }
    }
    protected class PendingActionMessage{
        public byte[] buffer;
        public int length;
        public int pendingTime;
        public MessageBuffer.MessageHeader messageHeader;

        public PendingActionMessage(byte[] data,int length,int pendingTime, MessageBuffer.MessageHeader messageHeader){
            this.buffer = data;
            this.length = length;
            this.pendingTime = pendingTime;
            this.messageHeader = messageHeader;
        }
    }

}
