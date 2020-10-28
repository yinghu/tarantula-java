package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class OnJoinedMessageHandler extends AbstractMessageHandler {
    private final int sessionId;
    public OnJoinedMessageHandler(GameChannelService gameService,int sessionId){
        super(gameService);
        this.sessionId = sessionId;
        this.ack = true;
    }

    @Override
    public int type() {
        return ON_JOINED;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        outboundMessage = new OutboundMessage();
        connectionId = pendingInboundMessage.connectionId();
        outboundMessage.ack(ack);
        outboundMessage.timestamp(pendingInboundMessage.timestamp());
        messageId = gameChannelService.messageId();
        outboundMessage.messageId(messageId);
        outboundMessage.sessionId(sessionId);
        outboundMessage.type(ON_JOINED);
        outboundMessage.sequence(pendingInboundMessage.sequence());
    }
    public void relay(){
        System.out.println("on joined->"+sessionId);
        super.relay();
    }

}
