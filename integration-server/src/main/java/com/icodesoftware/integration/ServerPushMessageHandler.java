package com.icodesoftware.integration;

import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;
import com.icodesoftware.util.FIFOBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class ServerPushMessageHandler extends AbstractMessageHandler {

    private FIFOBuffer<Integer> ackBuffer;

    public ServerPushMessageHandler(GameChannelService udpService){
        super(udpService);
        ackBuffer =  new FIFOBuffer(20,new Integer[20]);
    }
    @Override
    public int type() {
        return SERVER_PUSH;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        if(pendingInboundMessage.ack()) {
            ackBuffer.push(pendingInboundMessage.messageId());
            OutboundMessage ack = new OutboundMessage();
            ack.ack(false);
            ack.type(MessageHandler.ACK);
            ack.sequence(0);
            List<Integer> alist = ackBuffer.list(new ArrayList<>());
            DataBuffer dataBuffer = new DataBuffer();
            dataBuffer.putInt(alist.size());
            alist.forEach((a)-> dataBuffer.putInt(a));
            ack.payload(dataBuffer.toArray());
            gameChannelService.pendingOutbound(ByteBuffer.wrap(gameChannelService.encode(ack)), pendingInboundMessage.source());
        }
        DataBuffer dataBuffer = new DataBuffer(pendingInboundMessage.payload());
        System.out.println("SERVER PUSH->"+pendingInboundMessage.sequence()+">>>"+pendingInboundMessage.messageId()+">>"+dataBuffer.getLong()+">>"+dataBuffer.getUTF8());
    }
}
