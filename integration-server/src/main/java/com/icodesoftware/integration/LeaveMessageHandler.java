package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class LeaveMessageHandler extends AbstractMessageHandler {

    public LeaveMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return LEAVE;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(pendingInboundMessage.ack());
        pendingOutboundMessage.messageId(pendingInboundMessage.messageId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        GameChannel gameChannel = gameChannelService.gameChannel(pendingInboundMessage.connectionId());
        gameChannel.leave(pendingInboundMessage.sessionId(),pendingInboundMessage.source());
        OnLeftMessageHandler onLeftMessageHandler = new OnLeftMessageHandler(gameChannelService);
        onLeftMessageHandler.onMessage(pendingInboundMessage);
        byte[] pending = (gameChannelService.encode(pendingOutboundMessage));
        gameChannel.pending(pendingInboundMessage.sessionId(),pendingInboundMessage.messageId(),pending,onLeftMessageHandler);
        gameChannelService.pendingOutbound(pending,pendingInboundMessage.source());
        onLeftMessageHandler.relay();
    }
}
