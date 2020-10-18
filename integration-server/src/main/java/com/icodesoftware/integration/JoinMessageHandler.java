package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PayloadBuffer;
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
        PayloadBuffer data = new PayloadBuffer();
        if(this.gameChannelService.validateTicket(pendingInboundMessage.payload())){
            int sessionId = gameChannelService.sessionId();
            pendingOutboundMessage.sessionId(sessionId);
            data.putUTF8("accepted");
            pendingOutboundMessage.payload(data.toArray());
            gameChannelService.gameChannel(pendingInboundMessage.connectionId()).join(sessionId,pendingInboundMessage.source());
        }
        else{
            data.putUTF8("rejected");
            pendingOutboundMessage.payload(data.toArray());
        }
        this.gameChannelService.send(pendingOutboundMessage,pendingInboundMessage.source());
    }
}
