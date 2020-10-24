package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;

import java.nio.ByteBuffer;

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
    public void onMessage(InboundMessage pendingInboundMessage) {
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(true);
        pendingOutboundMessage.timestamp(pendingInboundMessage.timestamp());
        pendingOutboundMessage.messageId(pendingInboundMessage.messageId());
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        DataBuffer data = new DataBuffer();
        GameChannel gameChannel = gameChannelService.gameChannel(pendingInboundMessage.connectionId());
        if(this.gameChannelService.validateTicket(pendingInboundMessage.payload())){
            int sessionId = gameChannelService.sessionId();
            pendingOutboundMessage.sessionId(sessionId);
            data.putUTF8("accepted");
            pendingOutboundMessage.payload(data.toArray());
            gameChannel.join(sessionId,pendingInboundMessage.source());
            gameChannel.ack(sessionId,pendingInboundMessage.messageId(),pendingInboundMessage.source());
            gameChannel.relay(pendingInboundMessage.messageId(),true,pendingOutboundMessage);
        }
        else{
            data.putUTF8("rejected");
            pendingOutboundMessage.payload(data.toArray());
            ByteBuffer resp = this.gameChannelService.send(pendingOutboundMessage,pendingInboundMessage.source());
            gameChannel.pending(pendingInboundMessage.sessionId(),pendingInboundMessage.messageId(),resp);
        }
    }
}
