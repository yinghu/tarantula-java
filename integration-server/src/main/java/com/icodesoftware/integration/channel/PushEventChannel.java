package com.icodesoftware.integration.channel;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.Game;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.integration.OnKickedOffMessageHandler;
import com.icodesoftware.integration.udp.PendingMessage;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.*;
import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private final ConcurrentHashMap<SocketAddress,PendingSession> jIndex;

    private final MessageHandler joinMessageHandler;
    private final MessageHandler dischargeMessageHandler;
    private final MessageHandler serverPushMessageHandler;
    private final byte[] ping;

    private Game game;
    private Listener listener;
    private int totalRetries;

    public PushEventChannel(final long channelId,final GameChannelService gameChannelService){
        this.channelId = channelId;
        this.gameChannelService = gameChannelService;
        this.mSession = new ConcurrentHashMap<>();
        this.mMessage = new ConcurrentHashMap<>();
        this.mIndex = new ConcurrentHashMap<>();
        this.jIndex = new ConcurrentHashMap<>();
        this.joinMessageHandler = this.gameChannelService.messageHandler(MessageHandler.JOIN);
        this.dischargeMessageHandler = this.gameChannelService.messageHandler(MessageHandler.DISCHARGE);
        this.serverPushMessageHandler = this.gameChannelService.messageHandler(MessageHandler.SERVER_PUSH);
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.type(MessageHandler.PING);
        pendingOutboundMessage.sequence(0);
        ping = gameChannelService.encode(pendingOutboundMessage);
    }
    @Override
    public long channelId() {
        return channelId;
    }

    public void join(int seat,int sessionId,int[] messageRange,SocketAddress socketAddress){
        mSession.put(sessionId,new RemoteSession(seat,messageRange,socketAddress,jIndex.get(socketAddress).ackBuffer));
    }
    public void leave(int sessionId,SocketAddress socketAddress){
        if(mSession.containsKey(sessionId)&&mSession.get(sessionId).socketAddress.equals(socketAddress)){
            mSession.remove(sessionId);
            jIndex.remove(socketAddress);
            this.listener.onChannelClosed(this);
        }
    }
    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        if(mSession.containsKey(pendingInboundMessage.sessionId()) && mSession.get(pendingInboundMessage.sessionId()).validate(pendingInboundMessage.source())){
            if(pendingInboundMessage.ack()){//early ack to avoid retry from remote
                ack(pendingInboundMessage.sessionId(),pendingInboundMessage.messageId(),pendingInboundMessage.source());
            }
            MessageHandler messageHandler = gameChannelService.messageHandler(pendingInboundMessage.type());
            if(messageHandler!=null){
                if(!pendingInboundMessage.ack()){
                    messageHandler.onMessage(pendingInboundMessage);
                }
                else{
                    if(mIndex.putIfAbsent(pendingInboundMessage.messageId(),LocalDateTime.now(ZoneOffset.UTC))==null){
                        messageHandler.onMessage(pendingInboundMessage);
                    }
                }
            }
            else{
                dischargeMessageHandler.onMessage(pendingInboundMessage);
            }
        }
        else if(pendingInboundMessage.type()==MessageHandler.JOIN && jIndex.putIfAbsent(pendingInboundMessage.source(),new PendingSession())==null){
            joinMessageHandler.onMessage(pendingInboundMessage);
        }
        else if(pendingInboundMessage.type()==MessageHandler.ACK && jIndex.containsKey(pendingInboundMessage.source())) {
            PendingSession pendingSession = jIndex.get(pendingInboundMessage.source());
            if (pendingSession != null) {
                DataBuffer buffer = new DataBuffer(pendingInboundMessage.payload());
                var sz = buffer.getInt();
                for (int i = 0; i < sz; i++) {
                    if(pendingSession.messageId == buffer.getInt()){
                        jIndex.remove(pendingInboundMessage.source());
                    }
                }
            }
        }
        else if(pendingInboundMessage.type()==MessageHandler.SERVER_PUSH && mIndex.putIfAbsent(pendingInboundMessage.messageId(),LocalDateTime.now(ZoneOffset.UTC))==null){
            this.serverPushMessageHandler.onMessage(pendingInboundMessage);
        }
        else{
            dischargeMessageHandler.onMessage(pendingInboundMessage);
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
    public void relay(int sessionId,int messageId,boolean ack,MessageHandler messageHandler,OutboundMessage pendingOutboundMessage){
        byte[] outMessage = gameChannelService.encode(pendingOutboundMessage);
        RemoteSession remoteSession = mSession.get(sessionId);
        if(!ack){
            this.gameChannelService.pendingOutbound(ByteBuffer.wrap(outMessage),remoteSession.socketAddress);
        }
        else{
            ByteBuffer pending = ByteBuffer.wrap(outMessage);
            pending(sessionId,messageId,pending,messageHandler);
            this.gameChannelService.pendingOutbound(pending,remoteSession.socketAddress);
        }
    }
    public void ping(){
        ArrayList<Integer> kickOff = new ArrayList<>();
        mSession.forEach((k,v)->{
            if(v.pingPong.incrementAndGet()<5){
                this.gameChannelService.pendingOutbound(ByteBuffer.wrap(ping),v.socketAddress);
            }else{
                kickOff.add(k);
                mSession.remove(k);
                jIndex.remove(v.socketAddress);
            }
        });
        kickOff.forEach((k)->{
            OnKickedOffMessageHandler kickedOffMessageHandler = new OnKickedOffMessageHandler(this.gameChannelService,k,channelId);
            kickedOffMessageHandler.onMessage(null);
            kickedOffMessageHandler.relay();
        });
        kickOff.clear();
        mIndex.forEach((k,v)->{
            if(v.plusSeconds(10).isBefore(LocalDateTime.now(ZoneOffset.UTC))){
                mIndex.remove(k);
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
        int[] _retries = {0};
        this.mMessage.forEach((k,v)->{
            RemoteSession session = mSession.get(k.sessionId);
            if(session!=null&&checkExpired(v.timestamp,500)){
                v.timestamp = toUTCMilliseconds();
                v.data.flip();
                this.gameChannelService.pendingOutbound(v.data,v.source);
                _retries[0]++;
                v.retries--;
                if(v.retries<0){
                    mMessage.remove(k);
                }
            }
        });
        this.jIndex.forEach((k,v)->{
            if(v.pending.get()&&checkExpired(v.timestamp,500)){
                v.timestamp = toUTCMilliseconds();
                v.data.flip();
                this.gameChannelService.pendingOutbound(v.data,k);
                _retries[0]++;
                v.retries--;
                if(v.retries<0){
                    jIndex.remove(k);
                }
            }
        });
        totalRetries += _retries[0];
    }
    public int totalRetries(){
        return totalRetries;
    }
    public void pending(int sessionId, int messageId, ByteBuffer pending,MessageHandler callback){
        mMessage.put(new PendingMessageIndex(sessionId,messageId),new PendingMessage(pending,toUTCMilliseconds(),2,callback));
    }
    public void pending(SocketAddress socketAddress,int messageId,ByteBuffer pending){
        PendingSession pendingSession = jIndex.get(socketAddress);
        if(pendingSession!=null){
            pendingSession.messageId = messageId;
            pendingSession.data = pending;
            pendingSession.timestamp = toUTCMilliseconds();
            pendingSession.pending.set(true);
        }
    }
    public void onGame(Game game){
        this.game = game;
    }
    public Game onGame(){
        return this.game;
    }
    public void registerListener(Listener listener){
        this.listener = listener;
    }

    private boolean checkExpired(long timestamp,long pms){
        return toUTCMilliseconds()-timestamp>=pms;
    }
    private static long toUTCMilliseconds(){
        return LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
