package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class EchoMessageHandler extends AbstractMessageHandler {

    public EchoMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }
    @Override
    public int type() {
        return ECHO;
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
        //this.gameChannelService.pendingMessage(new PendingMessage(pendingOutboundMessage,pendingInboundMessage.source()));
    }
}
