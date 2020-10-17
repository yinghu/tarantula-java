package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;
import com.icodesoftware.protocol.PendingOutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class JoinMessageHandler implements MessageHandler {
    private final GameChannelService gameChannelService;
    public JoinMessageHandler(GameChannelService udpService){
        this.gameChannelService = udpService;
    }
    @Override
    public int type() {
        return JOIN;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {
        PendingOutboundMessage pendingOutboundMessage = new PendingOutboundMessage();
        pendingOutboundMessage.ack(true);
        pendingOutboundMessage.timestamp(pendingInboundMessage.timestamp());
        pendingOutboundMessage.messageId(pendingInboundMessage.messageId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        if(this.gameChannelService.validateTicket(pendingInboundMessage.payload())){
            pendingOutboundMessage.sessionId(gameChannelService.sessionId());
            pendingOutboundMessage.payload("ok".getBytes());
        }
        this.gameChannelService.send(pendingOutboundMessage,pendingInboundMessage.source());
    }
}
