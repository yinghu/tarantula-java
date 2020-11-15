package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class RelayMessageHandler extends AbstractMessageHandler {
     public RelayMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return RELAY;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(pendingInboundMessage.ack());
        pendingOutboundMessage.timestamp(pendingInboundMessage.timestamp());
        pendingOutboundMessage.messageId(pendingInboundMessage.messageId());
        pendingOutboundMessage.sessionId(pendingInboundMessage.sessionId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        pendingOutboundMessage.payload(pendingInboundMessage.payload());
        this.gameChannelService.gameChannel(pendingInboundMessage.connectionId()).relay(pendingInboundMessage.messageId(),pendingInboundMessage.ack(),null,pendingOutboundMessage);
    }
}
