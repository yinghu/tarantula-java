package com.icodesoftware.protocol;

import com.icodesoftware.util.TimeUtil;

import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UserChannel {

    private int channelId;

    protected ConcurrentHashMap<Integer,UserSession> userSessionIndex;
    protected Messenger messenger;
    protected AtomicInteger sequence;

    protected ConcurrentHashMap<MessageBuffer.MessageHeader,PendingAckMessage> pendingAckMessageIndex;

    private ArrayList<Integer> _offline;
    private ArrayList<MessageBuffer.MessageHeader> _retried;
    private MessageBuffer.MessageHeader pingHeader;


    public UserChannel(int channelId, Messenger messenger){
        this.channelId = channelId;
        this.messenger = messenger;
        this.userSessionIndex = new ConcurrentHashMap<>();
        this.pendingAckMessageIndex = new ConcurrentHashMap<>();
        this.sequence = new AtomicInteger(0);
        this._offline = new ArrayList<>();
        this._retried = new ArrayList<>();
        this.pingHeader = new MessageBuffer.MessageHeader();
        this.pingHeader.commandId = Messenger.PING;
        this.pingHeader.channelId = channelId;
    }

    public int channelId(){
        return this.channelId;
    }

    public void channelId(int channelId){
        this.channelId = channelId;
    }

    public final void onMessage(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer, SocketAddress source){
        UserSession userSession = userSessionIndex.computeIfAbsent(messageHeader.sessionId,k-> {
            if(messageHeader.commandId != Messenger.JOIN) return null;
            return this.validate(messageHeader, messageBuffer) ? new UserSession(k, source) : null;
        });
        if(userSession==null){
            return;
        }
        if(userSession.onJoin()){//first join call
            //server push onJoin 200
            if(messageHeader.ack) onAck(userSession,messageHeader.copy(),source);
            onJoin(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.commandId == Messenger.JOIN){//rejoin call
            if(!this.validate(messageHeader,messageBuffer)){
                //kickoff
                userSessionIndex.remove(messageHeader.sessionId);
                this.onTimeout(channelId,messageHeader.sessionId);
                return;
            }
            if(messageHeader.ack) onAck(userSession,messageHeader.copy(),source);
            userSession.source = source;
            userSession.onPing();
            onJoin(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.commandId == Messenger.ACK){
            //server clear on ack
            for(int i=0;i<MessageBuffer.PENDING_ACK_SIZE;i++){
                MessageBuffer.MessageHeader h = messageBuffer.readHeader();
                PendingAckMessage pendingAckMessage = pendingAckMessageIndex.get(h);
                if(pendingAckMessage == null) continue;
                pendingAckMessage.pendingAck--;
                if(pendingAckMessage.pendingAck<=0){
                    PendingAckMessage removed = pendingAckMessageIndex.remove(h);
                    if(removed!=null) messenger.buffer(removed.buffer);
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
            if(messageHeader.ack) onAck(userSession,messageHeader.copy(),source);
            onRequest(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.commandId == Messenger.LEAVE){
            if(messageHeader.ack) onAck(userSession,messageHeader.copy(),source);
            onLeave(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.commandId == Messenger.ACTION){
            if(messageHeader.ack) onAck(userSession,messageHeader.copy(),source);
            onAction(messageHeader,messageBuffer);
            return;
        }
        if(messageHeader.ack) onAck(userSession,messageHeader.copy(),source);
        onRelay(messageHeader,messageBuffer);
    }

    public final void onKickoff(){
        userSessionIndex.forEach((k,v)->{
            if(!v.online()) _offline.add(k);
        });
        _offline.forEach((i)-> {
            userSessionIndex.remove(i);
            this.onTimeout(channelId,i);
        });
        _offline.clear();
    }

    public final void onRetry(){
        _retried.clear();
        pendingAckMessageIndex.forEach((k,v)->{
            if(v.broadcasting){
                userSessionIndex.forEach((uk, uu) -> messenger.queue(v.buffer,v.length,uu.source));
            }
            else{
                UserSession userSession = userSessionIndex.get(v.sessionId);
                if(userSession!=null) messenger.queue(v.buffer,v.length,userSession.source);
            }
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
        long timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        MessageBuffer pingBuffer = messenger.messageBuffer();
        userSessionIndex.forEach((k,v)->{
            pingBuffer.reset();
            pingHeader.sessionId = k;
            pingBuffer.writeHeader(pingHeader);
            pingBuffer.writeLong(timestamp);
            pingBuffer.flip();
            messenger.queue(pingBuffer,v.source);
        });
        messenger.messageBuffer(pingBuffer);
    }

    public final void queue(int sessionId,MessageBuffer messageBuffer){
        UserSession userSession = userSessionIndex.get(sessionId);
        if(userSession==null) return;
        messenger.queue(messageBuffer,userSession.source);
    }

    public final void kickoff(int sessionId){
        userSessionIndex.remove(sessionId);
        this.onTimeout(channelId,sessionId);
    }

    public final void kickoff(){
        userSessionIndex.forEach((k,u)->{
            this.onTimeout(channelId,k);
        });
        userSessionIndex.clear();
    }

    private void onAck(UserSession userSession, MessageBuffer.MessageHeader messageHeader,SocketAddress source){
        userSession.pendingAck(messageHeader);
        List<MessageBuffer.MessageHeader> _acks = userSession.pendingAckList();
        MessageBuffer messageBuffer = this.messenger.messageBuffer();
        messageBuffer.reset();
        MessageBuffer.MessageHeader ackHeader = new MessageBuffer.MessageHeader();
        ackHeader.commandId = Messenger.ACK;
        messageBuffer.writeHeader(ackHeader);
        _acks.forEach((mh)->messageBuffer.writeHeader(mh));
        messageBuffer.flip();
        messenger.queue(messageBuffer,source);
        messenger.messageBuffer(messageBuffer);
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
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader.sessionId,buffer,length);
        userSessionIndex.forEach((sid,session)->{
            messenger.queue(pendingAckMessage.buffer,pendingAckMessage.length,session.source);
            pendingAckMessage.pendingAck++;
        });
        pendingAckMessageIndex.put(messageHeader,pendingAckMessage);
    }

    protected void onLeave(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        userSessionIndex.remove(messageHeader.sessionId);
        this.onTimeout(channelId,messageHeader.sessionId);
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
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader.sessionId,buffer,length);
        userSessionIndex.forEach((sid,session)->{
            messenger.queue(pendingAckMessage.buffer,pendingAckMessage.length,session.source);
            pendingAckMessage.pendingAck++;
        });
        pendingAckMessageIndex.put(messageHeader,pendingAckMessage);
    }

    protected void onRelay(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        messageBuffer.rewind();
        byte[] buffer = messenger.buffer();
        int length = messageBuffer.toArray(buffer);
        int[] pendingAck ={0};
        userSessionIndex.forEach((sid,session)->{
            if(!messageHeader.broadcasting){
                if(messageHeader.sessionId != sid){
                    messenger.queue(buffer,length,session.source);
                    pendingAck[0]++;
                }
            }
            else{
                messenger.queue(buffer,length,session.source);
                pendingAck[0]++;
            }
        });
        if(!messageHeader.ack || pendingAck[0]==0) return;
        PendingAckMessage pendingAckMessage = new PendingAckMessage(messageHeader.sessionId,buffer,length);
        pendingAckMessage.pendingAck = pendingAck[0];
        pendingAckMessageIndex.put(messageHeader,pendingAckMessage);
    }

    protected void onRequest(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){

    }

    protected void onAction(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){

    }

    protected boolean validate(MessageBuffer.MessageHeader messageHeader,MessageBuffer messageBuffer){
        return false;
    }

    protected void onTimeout(int channelId,int sessionId){

    }

    protected class PendingAckMessage{
        public int sessionId;
        public byte[] buffer;
        public int length;
        public int retries = MessageBuffer.RETRIES;
        public int pendingAck;
        public boolean broadcasting;
        public PendingAckMessage(int sessionId,byte[] buffer,int length){
            this.sessionId = sessionId;
            this.buffer = buffer;
            this.length = length;
            this.broadcasting = true;
        }
        public PendingAckMessage(int sessionId,byte[] buffer,int length,boolean broadcasting){
            this.sessionId = sessionId;
            this.buffer = buffer;
            this.length = length;
            this.broadcasting = broadcasting;
        }
    }
}
