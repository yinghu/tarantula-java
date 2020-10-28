package com.icodesoftware.integration.channel;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.*;
import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public class PushEventChannel implements GameChannel {

    private static TarantulaLogger log = JDKLogger.getLogger(PushEventChannel.class);

    private final long channelId;
    private final GameChannelService gameChannelService;
    private final ConcurrentHashMap<Integer, RemoteSession> mSession;
    private final ConcurrentHashMap<PendingMessageIndex, PendingMessage> mMessage;

    private final MessageHandler joinMessageHandler;

    public PushEventChannel(final long channelId,final GameChannelService gameChannelService){
        this.channelId = channelId;
        this.gameChannelService = gameChannelService;
        this.mSession = new ConcurrentHashMap<>();
        this.mMessage = new ConcurrentHashMap<>();
        this.joinMessageHandler = this.gameChannelService.messageHandler(MessageHandler.JOIN);
    }
    @Override
    public long channelId() {
        return channelId;
    }
    public void join(int sessionId,SocketAddress socketAddress){
        mSession.put(sessionId,new RemoteSession(socketAddress));
    }
    public void leave(int sessionId,SocketAddress socketAddress){
        if(mSession.containsKey(sessionId)&&mSession.get(sessionId).socketAddress.equals(socketAddress)){
            mSession.remove(sessionId);
        }
    }
    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        if(pendingInboundMessage.type()!=MessageHandler.JOIN
                &&mSession.containsKey(pendingInboundMessage.sessionId())
                &&mSession.get(pendingInboundMessage.sessionId()).socketAddress.equals(pendingInboundMessage.source())){
            MessageHandler messageHandler = gameChannelService.messageHandler(pendingInboundMessage.type());
            if(messageHandler!=null){
                if(pendingInboundMessage.ack()){
                    ack(pendingInboundMessage.sessionId(),pendingInboundMessage.messageId(),pendingInboundMessage.source());
                }
                messageHandler.onMessage(pendingInboundMessage);
            }
            else{
                log.warn("no message handler registered ->"+pendingInboundMessage.type());
            }
        }
        else if(pendingInboundMessage.type()==MessageHandler.JOIN){
            joinMessageHandler.onMessage(pendingInboundMessage);
        }
        else{
            //log.warn("Discharging message->"+pendingInboundMessage.connectionId()+"/"+pendingInboundMessage.type()+"/"+pendingInboundMessage.messageId()+"/"+pendingInboundMessage.sessionId());
        }
    }
    public void ack(int sessionId,int messageId,SocketAddress source){
        RemoteSession remoteSession = mSession.get(sessionId);
        if(remoteSession==null){
            return;
        }
        OutboundMessage ack = new OutboundMessage();
        ack.ack(false);
        ack.type(MessageHandler.ACK);
        ack.sequence(0);
        DataBuffer dataBuffer = new DataBuffer();
        FIFOBuffer<Integer> buffer = remoteSession.ackBuffer;
        buffer.push(messageId);
        List<Integer> alist = buffer.list(new ArrayList<>());
        dataBuffer.putInt(alist.size());
        alist.forEach((mid)->{dataBuffer.putInt(mid);});
        ack.payload(dataBuffer.toArray());
        gameChannelService.send(ack,source);
    }
    public void ack(int sessionId,int messageId){
        PendingMessage pendingMessage = mMessage.remove(new PendingMessageIndex(sessionId,messageId));
        if(pendingMessage!=null&&pendingMessage.callback!=null){
            pendingMessage.callback.relay();
        }
    }
    public void relay(int messageId,boolean ack,OutboundMessage pendingOutboundMessage){
        this.mSession.forEach((k,v)->{
            ByteBuffer resp = this.gameChannelService.send(pendingOutboundMessage,v.socketAddress);
            if(ack){
                mMessage.put(new PendingMessageIndex(k,messageId),new PendingMessage(resp,toUTCMilliseconds(),2));
            }
        });
    }
    public void ping(){
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.type(MessageHandler.PING);
        pendingOutboundMessage.sequence(0);
        mSession.forEach((k,v)->{
            if(v.pingPong.incrementAndGet()<5){
                this.gameChannelService.send(pendingOutboundMessage,v.socketAddress);
            }else{
                mSession.remove(k);
                log.warn("session kicked off ->"+k);
            }
        });
    }
    public void pong(int sessionId){
        RemoteSession remoteSession = mSession.get(sessionId);
        if(remoteSession!=null){
            remoteSession.pingPong.decrementAndGet();
        }
    }
    public void retry(){
        this.mMessage.forEach((k,v)->{
            RemoteSession session = mSession.get(k.sessionId);
            if(session!=null&&checkExpired(v.timestamp,500)){
                log.warn("RETRY->"+v.retries+"/"+(toUTCMilliseconds()-v.timestamp)+"<>"+k.toString());
                v.timestamp = toUTCMilliseconds();
                v.data.flip();
                this.gameChannelService.retry(v.data,session.socketAddress);
                v.retries--;
                if(v.retries<0){
                    mMessage.remove(k);
                }
            }
        });
    }
    public void pending(int sessionId, int messageId, ByteBuffer pending,MessageHandler callback){
        mMessage.put(new PendingMessageIndex(sessionId,messageId),new PendingMessage(pending,toUTCMilliseconds(),2,callback));
    }
    public void pending(int sessionId, int messageId, ByteBuffer pending){
        mMessage.put(new PendingMessageIndex(sessionId,messageId),new PendingMessage(pending,toUTCMilliseconds(),2));
    }
    private boolean checkExpired(long timestamp,long pms){
        return toUTCMilliseconds()-timestamp>=pms;
    }
    private static long toUTCMilliseconds(){
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
