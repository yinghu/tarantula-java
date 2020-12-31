package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class DestroyMessageHandler extends AbstractMessageHandler {
     public DestroyMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return DESTROY;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        GameChannel _gameChannel = gameChannelService.gameChannel(pendingInboundMessage.connectionId());
        if(!_gameChannel.onGame().onDestroy(pendingInboundMessage)){
            return;
        }
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(pendingInboundMessage.ack());
        pendingOutboundMessage.messageId(pendingInboundMessage.messageId());
        pendingOutboundMessage.sessionId(pendingInboundMessage.sessionId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        pendingOutboundMessage.payload(pendingInboundMessage.payload());
        _gameChannel.relay(pendingInboundMessage.messageId(),pendingInboundMessage.ack(),null,pendingOutboundMessage);
    }
}
