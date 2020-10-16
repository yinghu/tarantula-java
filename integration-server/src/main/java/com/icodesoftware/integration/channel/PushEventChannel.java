package com.icodesoftware.integration.channel;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;

/**
 * Created by yinghu lu on 10/16/2020.
 */
public class PushEventChannel implements GameChannel {

    private static TarantulaLogger log = JDKLogger.getLogger(PushEventChannel.class);

    private final long channelId;
    private final GameChannelService gameChannelService;

    public PushEventChannel(final long channelId,final GameChannelService gameChannelService){
        this.channelId = channelId;
        this.gameChannelService = gameChannelService;
    }
    @Override
    public long channelId() {
        return channelId;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {
        MessageHandler messageHandler = gameChannelService.messageHandler(pendingInboundMessage.type());
        if(messageHandler!=null){
            messageHandler.onMessage(pendingInboundMessage);
        }
        else{
            log.warn("no message handler registered ->"+pendingInboundMessage.type());
        }
    }
}
