package com.icodesoftware.integration;

import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

import java.nio.ByteBuffer;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class ServerPushMessageHandler extends AbstractMessageHandler {

    public ServerPushMessageHandler(GameChannelService udpService){
        super(udpService);
    }
    @Override
    public int type() {
        return SERVER_PUSH;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        if(pendingInboundMessage.ack()) {
            OutboundMessage ack = new OutboundMessage();
            ack.ack(false);
            ack.type(MessageHandler.ACK);
            ack.sequence(0);
            DataBuffer dataBuffer = new DataBuffer();
            dataBuffer.putInt(1);
            dataBuffer.putInt(pendingInboundMessage.messageId());
            ack.payload(dataBuffer.toArray());
            gameChannelService.pendingOutbound(ByteBuffer.wrap(gameChannelService.encode(ack)), pendingInboundMessage.source());
        }
        DataBuffer dataBuffer = new DataBuffer(pendingInboundMessage.payload());
        System.out.println("SERVER PUSH->"+pendingInboundMessage.messageId()+dataBuffer.getUTF8()+">>"+dataBuffer.getUTF8());
    }
}
