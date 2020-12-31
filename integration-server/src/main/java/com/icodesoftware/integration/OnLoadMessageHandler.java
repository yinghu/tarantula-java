package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class OnLoadMessageHandler extends AbstractMessageHandler {
     public OnLoadMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return ON_LOAD;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
         OutboundMessage pendingOutboundMessage = new OutboundMessage();
         pendingOutboundMessage.ack(pendingInboundMessage.ack());
         int mid = gameChannelService.messageId();
         pendingOutboundMessage.messageId(mid);
         pendingOutboundMessage.sessionId(pendingInboundMessage.sessionId());
         pendingOutboundMessage.type(pendingInboundMessage.type());
         pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
         pendingOutboundMessage.payload(pendingInboundMessage.payload());
         this.gameChannelService.gameChannel(pendingInboundMessage.connectionId()).relay(mid,pendingInboundMessage.ack(),null,pendingOutboundMessage);
    }
}
