package com.icodesoftware.integration.channel;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.*;
import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public class PushEventChannel implements GameChannel {

    private static TarantulaLogger log = JDKLogger.getLogger(PushEventChannel.class);

    private final long channelId;
    private final GameChannelService gameChannelService;
    private final ConcurrentHashMap<Integer, RemoteSession> mSession;
    private final ConcurrentHashMap<PendingMessageIndex, PendingMessage> mMessage;
    private final ConcurrentHashMap<Integer,LocalDateTime> mIndex;
    private final ConcurrentHashMap<SocketAddress,LocalDateTime> jIndex;

    private final MessageHandler joinMessageHandler;
    private final byte[] ping;

    public PushEventChannel(final long channelId,final GameChannelService gameChannelService){
        this.channelId = channelId;
        this.gameChannelService = gameChannelService;
        this.mSession = new ConcurrentHashMap<>();
        this.mMessage = new ConcurrentHashMap<>();
        this.mIndex = new ConcurrentHashMap<>();
        this.jIndex = new ConcurrentHashMap<>();
        this.joinMessageHandler = this.gameChannelService.messageHandler(MessageHandler.JOIN);
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.type(MessageHandler.PING);
        pendingOutboundMessage.sequence(0);
        ping = gameChannelService.encode(pendingOutboundMessage);
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
            jIndex.remove(socketAddress);
        }
    }
    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        if(pendingInboundMessage.type()!=MessageHandler.JOIN
                &&mSession.containsKey(pendingInboundMessage.sessionId())
                &&mSession.get(pendingInboundMessage.sessionId()).socketAddress.equals(pendingInboundMessage.source())){
            MessageHandler messageHandler = gameChannelService.messageHandler(pendingInboundMessage.type());
            if(messageHandler!=null){
                if(!pendingInboundMessage.ack()){
                    messageHandler.onMessage(pendingInboundMessage);
                }
                else{
                    ack(pendingInboundMessage.sessionId(),pendingInboundMessage.messageId(),pendingInboundMessage.source());
                    if(mIndex.putIfAbsent(pendingInboundMessage.messageId(),LocalDateTime.now(ZoneOffset.UTC))==null){
                       messageHandler.onMessage(pendingInboundMessage);
                    }
                }
            }
            else{
                log.warn("no message handler registered ->"+pendingInboundMessage.type());
            }
        }
        else if(pendingInboundMessage.type()==MessageHandler.JOIN){
            if(jIndex.putIfAbsent(pendingInboundMessage.source(),LocalDateTime.now(ZoneOffset.UTC))==null){
                joinMessageHandler.onMessage(pendingInboundMessage);
            }
        }
        else{
            log.warn("Discharging message->"+pendingInboundMessage.connectionId()+"/"+pendingInboundMessage.type()+"/"+pendingInboundMessage.messageId()+"/"+pendingInboundMessage.sessionId());
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
        gameChannelService.pendingOutbound(ByteBuffer.wrap(gameChannelService.encode(ack)),source);
    }
    public void ack(int sessionId,int messageId){
        PendingMessage pendingMessage = mMessage.remove(new PendingMessageIndex(sessionId,messageId));
        if(pendingMessage!=null&&pendingMessage.callback!=null){
            pendingMessage.callback.relay();
        }
    }
    public void relay(int messageId,boolean ack,MessageHandler messageHandler,OutboundMessage pendingOutboundMessage){
        byte[] outMessage = gameChannelService.encode(pendingOutboundMessage);
        this.mSession.forEach((k,v)->{
            if(!ack){
                this.gameChannelService.pendingOutbound(ByteBuffer.wrap(outMessage),v.socketAddress);
            }
            else{
                ByteBuffer pending = ByteBuffer.wrap(outMessage);
                pending(k,messageId,pending,messageHandler);
                this.gameChannelService.pendingOutbound(pending,v.socketAddress);
            }
        });
    }
    public void ping(){
        mSession.forEach((k,v)->{
            if(v.pingPong.incrementAndGet()<5){
                this.gameChannelService.pendingOutbound(ByteBuffer.wrap(ping),v.socketAddress);
            }else{
                mSession.remove(k);
                log.warn("session kicked off ->"+k);
            }
        });
        //mIndex.forEach((k,v)->{
            //log.warn("index->"+v.format(DateTimeFormatter.ISO_DATE_TIME));
        //});
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
                v.timestamp = toUTCMilliseconds();
                v.data.flip();
                this.gameChannelService.pendingOutbound(v.data,v.source);
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
    private boolean checkExpired(long timestamp,long pms){
        return toUTCMilliseconds()-timestamp>=pms;
    }
    private static long toUTCMilliseconds(){
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
