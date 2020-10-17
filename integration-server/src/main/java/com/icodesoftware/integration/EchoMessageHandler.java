package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class EchoMessageHandler implements MessageHandler {
    private final GameChannelService gameChannelService;
    public EchoMessageHandler(GameChannelService udpService){
        this.gameChannelService = udpService;
    }
    @Override
    public int type() {
        return ECHO;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {
        PendingOutboundMessage pendingOutboundMessage = new PendingOutboundMessage();
        pendingOutboundMessage.ack(pendingInboundMessage.ack());
        pendingOutboundMessage.timestamp(pendingInboundMessage.timestamp());
        pendingOutboundMessage.messageId(pendingInboundMessage.messageId());
        pendingOutboundMessage.sessionId(pendingInboundMessage.sessionId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        pendingOutboundMessage.payload(pendingInboundMessage.payload());
        this.gameChannelService.send(pendingOutboundMessage,pendingInboundMessage.source());
    }
}
