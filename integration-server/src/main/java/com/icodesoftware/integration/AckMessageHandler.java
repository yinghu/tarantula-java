package com.icodesoftware.integration;

import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class AckMessageHandler implements MessageHandler {
    private final GameChannelService gameChannelService;
    public AckMessageHandler(GameChannelService gameService){
        this.gameChannelService = gameService;
    }

    @Override
    public int type() {
        return ACK;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {
        DataBuffer buffer = new DataBuffer(pendingInboundMessage.payload());
        var sz = buffer.getInt();
        for (int i = 0; i < sz; i++)
        {
            //System.out.println("Ack Id->"+buffer.getInt());
        }
        //System.out.println("ACK END");
    }
}
