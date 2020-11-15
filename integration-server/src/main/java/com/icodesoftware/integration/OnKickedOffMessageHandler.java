package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class OnKickedOffMessageHandler extends AbstractMessageHandler {

    private int sessionId;

    public OnKickedOffMessageHandler(GameChannelService gameChannelService,int sessionId,long connectionId){
        super(gameChannelService);
        this.sessionId = sessionId;
        this.ack = true;
        this.connectionId = connectionId;
    }

    @Override
    public int type() {
        return ON_KICKED_OFF;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        outboundMessage = new OutboundMessage();
        outboundMessage.ack(ack);
        outboundMessage.sessionId(sessionId);
        messageId = gameChannelService.messageId();
        outboundMessage.messageId(messageId);
        outboundMessage.type(MessageHandler.ON_KICKED_OFF);
    }
}
