package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/26/2020.
 */
abstract public class AbstractMessageHandler implements MessageHandler {

    protected final GameChannelService gameChannelService;
    protected OutboundMessage outboundMessage;
    protected long connectionId;
    protected int messageId;
    protected boolean ack;
    public AbstractMessageHandler(final GameChannelService gameChannelService){
        this.gameChannelService = gameChannelService;
    }
    @Override
    public void relay() {
        if(outboundMessage!=null){
            this.gameChannelService.gameChannel(connectionId).relay(messageId,ack,outboundMessage);
        }
    }
}
