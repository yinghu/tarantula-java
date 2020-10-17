package com.icodesoftware.integration.channel;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
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
    private final AtomicInteger sessionId;
    public PushEventChannel(final long channelId,final GameChannelService gameChannelService){
        this.channelId = channelId;
        this.gameChannelService = gameChannelService;
        this.mSockets = new ConcurrentHashMap<>();
        this.sessionId = new AtomicInteger(0);
    }
    @Override
    public long channelId() {
        return channelId;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {
        log.warn("SESSION ID->"+pendingInboundMessage.sessionId());
        MessageHandler messageHandler = gameChannelService.messageHandler(pendingInboundMessage.type());
        if(messageHandler!=null){
            messageHandler.onMessage(pendingInboundMessage);
        }
        else{
            log.warn("no message handler registered ->"+pendingInboundMessage.type());
        }
    }
}
