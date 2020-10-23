package com.icodesoftware.integration.channel;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;
import com.icodesoftware.util.FIFOBuffer;

import java.net.SocketAddress;
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
    private final ConcurrentHashMap<PendingMessageIndex,PendingOutboundMessage> mIndex;

    private final MessageHandler joinMessageHandler;
    private final MessageHandler ackMessageHandler;
    private final MessageHandler pingMessageHandler;
    public PushEventChannel(final long channelId,final GameChannelService gameChannelService){
        this.channelId = channelId;
        this.gameChannelService = gameChannelService;
        this.mSession = new ConcurrentHashMap<>();
        this.mIndex = new ConcurrentHashMap<>();
        this.joinMessageHandler = this.gameChannelService.messageHandler(MessageHandler.JOIN);
        this.ackMessageHandler = this.gameChannelService.messageHandler(MessageHandler.ACK);
        this.pingMessageHandler = this.gameChannelService.messageHandler(MessageHandler.PING);
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
    public void onMessage(PendingInboundMessage pendingInboundMessage) {
        if(pendingInboundMessage.type()!=MessageHandler.JOIN
                &&mSession.containsKey(pendingInboundMessage.sessionId())
                &&mSession.get(pendingInboundMessage.sessionId()).socketAddress.equals(pendingInboundMessage.source())){
            MessageHandler messageHandler = gameChannelService.messageHandler(pendingInboundMessage.type());
            if(messageHandler!=null){
                messageHandler.onMessage(pendingInboundMessage);
                if(pendingInboundMessage.ack()){
                    ack(pendingInboundMessage.sessionId(),pendingInboundMessage.messageId(),pendingInboundMessage.source());
                }
            }
            else{
                log.warn("no message handler registered ->"+pendingInboundMessage.type());
            }
        }
        else if(pendingInboundMessage.type()==MessageHandler.JOIN){
            joinMessageHandler.onMessage(pendingInboundMessage);
        }
        else{
            log.warn("Discharging message->"+pendingInboundMessage.connectionId()+"/"+pendingInboundMessage.type());
        }
    }
    public void ack(int sessionId,int messageId,SocketAddress source){
        PendingOutboundMessage ack = new PendingOutboundMessage();
        ack.type(MessageHandler.ACK);
        ack.sequence(0);
        DataBuffer dataBuffer = new DataBuffer();
        RemoteSession remoteSession = mSession.get(sessionId);
        FIFOBuffer<Integer> buffer = remoteSession.ackBuffer;
        buffer.push(messageId);
        List<Integer> alist = buffer.list(new ArrayList<>());
        dataBuffer.putInt(alist.size());
        alist.forEach((mid)->{dataBuffer.putInt(mid);});
        ack.payload(dataBuffer.toArray());
        gameChannelService.send(ack,source);
    }
    public void ack(int sessionId,int messageId){
        log.warn("ACK->"+sessionId+"///"+messageId);
    }
    public void send(PendingOutboundMessage pendingOutboundMessage){
        this.mSession.forEach((k,v)->{
            this.gameChannelService.send(pendingOutboundMessage,v.socketAddress);
        });
    }
    public void ping(){
        PendingOutboundMessage pendingOutboundMessage = new PendingOutboundMessage();
        pendingOutboundMessage.type(MessageHandler.PING);
        pendingOutboundMessage.sequence(0);
        mSession.forEach((k,v)->{
            this.gameChannelService.send(pendingOutboundMessage,v.socketAddress);
        });
    }
}
