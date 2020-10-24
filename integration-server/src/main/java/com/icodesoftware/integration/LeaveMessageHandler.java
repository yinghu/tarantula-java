package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class LeaveMessageHandler implements MessageHandler {
    private final GameChannelService gameChannelService;
    public LeaveMessageHandler(GameChannelService gameService){
        this.gameChannelService = gameService;
    }

    @Override
    public int type() {
        return LEAVE;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(true);
        pendingOutboundMessage.timestamp(pendingInboundMessage.timestamp());
        pendingOutboundMessage.messageId(pendingInboundMessage.messageId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        gameChannelService.gameChannel(pendingInboundMessage.connectionId()).leave(pendingInboundMessage.sessionId(),pendingInboundMessage.source());
        this.gameChannelService.send(pendingOutboundMessage,pendingInboundMessage.source());
    }
}
