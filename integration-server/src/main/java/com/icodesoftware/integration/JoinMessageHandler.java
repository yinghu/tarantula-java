package com.icodesoftware.integration;

import com.icodesoftware.protocol.*;


/**
 * Created by yinghu lu on 10/7/2020.
 */
public class JoinMessageHandler extends AbstractMessageHandler {


    public JoinMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
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
        DataBuffer buffer = new DataBuffer(pendingInboundMessage.payload());
        if(this.gameChannelService.validateTicket(buffer.getInt(),buffer.getUTF8(),buffer.getUTF8())){
            int sessionId = gameChannelService.sessionId();
            int[] mid = gameChannelService.messageIdRange();
            pendingOutboundMessage.sessionId(sessionId);
            data.putByte((byte)1);
            data.putInt(mid[0]);
            data.putInt(mid[1]);
            pendingOutboundMessage.payload(data.toArray());
            gameChannel.join(buffer.getInt(),sessionId,mid,pendingInboundMessage.source());
            gameChannel.ack(sessionId,pendingInboundMessage.messageId(),pendingInboundMessage.source());
            OnJoinedMessageHandler onJoinedMessageHandler = new OnJoinedMessageHandler(gameChannelService,sessionId);
            onJoinedMessageHandler.onMessage(pendingInboundMessage);
            byte[] outMessage = (gameChannelService.encode(pendingOutboundMessage));
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
            gameChannelService.pendingOutbound((gameChannelService.encode(ack)),pendingInboundMessage.source());
            data.putByte((byte)0);
            pendingOutboundMessage.payload(data.toArray());
            byte[] outMessage = (gameChannelService.encode(pendingOutboundMessage));
            gameChannel.pending(pendingInboundMessage.source(),messageId,outMessage);
            gameChannelService.pendingOutbound(outMessage,pendingInboundMessage.source());
        }
    }
}
