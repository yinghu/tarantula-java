package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class CollisionMessageHandler extends AbstractMessageHandler {

    public CollisionMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return COLLISION;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.ack(pendingInboundMessage.ack());
        outboundMessage.type(MessageHandler.ON_COLLISION);
        outboundMessage.sequence(pendingInboundMessage.sequence());
        int mid = gameChannelService.messageId();
        
        outboundMessage.messageId(mid);
        gameChannelService.gameChannel(pendingInboundMessage.connectionId()).relay(mid,pendingInboundMessage.ack(),null,outboundMessage);
    }
}
