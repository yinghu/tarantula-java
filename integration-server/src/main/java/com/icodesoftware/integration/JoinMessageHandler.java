package com.icodesoftware.integration;

import com.icodesoftware.protocol.*;

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
            gameChannel.join(sessionId,mid,pendingInboundMessage.source());
            gameChannel.ack(sessionId,pendingInboundMessage.messageId(),pendingInboundMessage.source());
            OnJoinedMessageHandler onJoinedMessageHandler = new OnJoinedMessageHandler(gameChannelService,sessionId);
            onJoinedMessageHandler.onMessage(pendingInboundMessage);
            ByteBuffer outMessage = ByteBuffer.wrap(gameChannelService.encode(pendingOutboundMessage));
            gameChannel.pending(sessionId,messageId,outMessage,onJoinedMessageHandler);
            gameChannelService.pendingOutbound(outMessage,pendingInboundMessage.source());
        }
        else{
            OutboundMessage ack = new OutboundMessage();
            ack.ack(false);
            ack.type(MessageHandler.ACK);
            ack.sequence(0);
            DataBuffer dataBuffer = new DataBuffer();
            dataBuffer.putInt(1);
            dataBuffer.putInt(pendingInboundMessage.messageId());
            ack.payload(dataBuffer.toArray());
            gameChannelService.pendingOutbound(ByteBuffer.wrap(gameChannelService.encode(ack)),pendingInboundMessage.source());
            data.putUTF8("rejected");
            pendingOutboundMessage.payload(data.toArray());
            ByteBuffer outMessage = ByteBuffer.wrap(gameChannelService.encode(pendingOutboundMessage));
            gameChannel.pending(pendingInboundMessage.source(),messageId,outMessage);
            gameChannelService.pendingOutbound(outMessage,pendingInboundMessage.source());
        }
    }
}
