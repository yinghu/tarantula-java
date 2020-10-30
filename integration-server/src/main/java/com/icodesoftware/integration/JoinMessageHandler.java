package com.icodesoftware.integration;

import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.OutboundMessage;
import com.icodesoftware.protocol.PendingMessage;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class JoinMessageHandler extends AbstractMessageHandler {


    public JoinMessageHandler(GameChannelService udpService){
        super(udpService);
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
        int messageId = gameChannelService.messageId();
        pendingOutboundMessage.messageId(messageId);
        pendingOutboundMessage.type(pendingInboundMessage.type());
        pendingOutboundMessage.sequence(pendingInboundMessage.sequence());
        DataBuffer data = new DataBuffer();
        GameChannel gameChannel = gameChannelService.gameChannel(pendingInboundMessage.connectionId());
        if(this.gameChannelService.validateTicket(pendingInboundMessage.payload())){
            int sessionId = gameChannelService.sessionId();
            int[] mid = gameChannelService.messageIdRange();
            pendingOutboundMessage.sessionId(sessionId);
            data.putUTF8("accepted");
            data.putInt(mid[0]);
            data.putInt(mid[1]);
            pendingOutboundMessage.payload(data.toArray());
            gameChannel.join(sessionId,pendingInboundMessage.source());
            gameChannel.ack(sessionId,pendingInboundMessage.messageId(),pendingInboundMessage.source());
            OnJoinedMessageHandler onJoinedMessageHandler = new OnJoinedMessageHandler(gameChannelService,sessionId);
            onJoinedMessageHandler.onMessage(pendingInboundMessage);
            PendingMessage pendingMessage = new PendingMessage(pendingOutboundMessage,pendingInboundMessage.source(),pendingInboundMessage.connectionId(),sessionId,messageId,true,onJoinedMessageHandler);
            gameChannelService.pendingMessage(pendingMessage);
        }
        else{
            data.putUTF8("rejected");
            pendingOutboundMessage.payload(data.toArray());
            PendingMessage pendingMessage = new PendingMessage(pendingOutboundMessage,pendingInboundMessage.source(),pendingInboundMessage.connectionId(),0,messageId,true);
            gameChannelService.pendingMessage(pendingMessage);
            //ByteBuffer resp = this.gameChannelService.send(pendingOutboundMessage,pendingInboundMessage.source());
            //gameChannel.ack(sessionId,pendingInboundMessage.messageId(),pendingInboundMessage.source());
            //gameChannel.pending(pendingInboundMessage.sessionId(),pendingInboundMessage.messageId(),resp);
        }
    }
}
