package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class OnLeftMessageHandler extends AbstractMessageHandler {
    public OnLeftMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return ON_LEFT;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        outboundMessage = new OutboundMessage();
        messageId = gameChannelService.messageId();
        connectionId = pendingInboundMessage.connectionId();
        ack = pendingInboundMessage.ack();
        outboundMessage.ack(ack);
        outboundMessage.messageId(messageId);
        outboundMessage.sessionId(pendingInboundMessage.sessionId());
        outboundMessage.type(ON_LEFT);
        outboundMessage.sequence(pendingInboundMessage.sequence());
        outboundMessage.payload(pendingInboundMessage.payload());
    }
}
