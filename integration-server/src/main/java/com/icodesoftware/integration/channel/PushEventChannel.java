package com.icodesoftware.integration.channel;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.integration.JoinMessageHandler;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public class PushEventChannel implements GameChannel {

    private static TarantulaLogger log = JDKLogger.getLogger(PushEventChannel.class);

    private final long channelId;
    private final GameChannelService gameChannelService;
    private final ConcurrentHashMap<Integer, SocketAddress> mSockets;

    private final MessageHandler joinMessageHandler;
    private final MessageHandler ackMessageHandler;
    public PushEventChannel(final long channelId,final GameChannelService gameChannelService){
        this.channelId = channelId;
        this.gameChannelService = gameChannelService;
        this.mSockets = new ConcurrentHashMap<>();
        this.joinMessageHandler = this.gameChannelService.messageHandler(MessageHandler.JOIN);
        this.ackMessageHandler = this.gameChannelService.messageHandler(MessageHandler.ACK);
    }
    @Override
    public long channelId() {
        return channelId;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {
        if(pendingInboundMessage.type()!=MessageHandler.JOIN
                &&mSockets.containsKey(pendingInboundMessage.sessionId())
                &&mSockets.get(pendingInboundMessage.sessionId()).equals(pendingInboundMessage.source())){
            MessageHandler messageHandler = gameChannelService.messageHandler(pendingInboundMessage.type());
            if(messageHandler!=null){
                messageHandler.onMessage(pendingInboundMessage);
                if(pendingInboundMessage.ack()){
                    ackMessageHandler.onMessage(pendingInboundMessage);
                }
            }
            else{
                log.warn("no message handler registered ->"+pendingInboundMessage.type());
            }
        }
        else{
             joinMessageHandler.onMessage(pendingInboundMessage);
        }
    }

}
