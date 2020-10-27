package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class OnJoinedMessageHandler extends AbstractMessageHandler {

    public OnJoinedMessageHandler(GameChannelService gameService){
        super(gameService);
    }

    @Override
    public int type() {
        return ON_JOINED;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        outboundMessage = new OutboundMessage();
        ack = pendingInboundMessage.ack();
        connectionId = pendingInboundMessage.connectionId();
        outboundMessage.ack(ack);
        outboundMessage.timestamp(pendingInboundMessage.timestamp());
        messageId = gameChannelService.messageId();
        outboundMessage.messageId(messageId);
        outboundMessage.sessionId(pendingInboundMessage.sessionId());
        outboundMessage.type(ON_JOINED);
        outboundMessage.sequence(pendingInboundMessage.sequence());
        outboundMessage.payload(pendingInboundMessage.payload());
    }

}
