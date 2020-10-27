package com.icodesoftware.integration;

import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;



/**
 * Created by yinghu lu on 10/7/2020.
 */
public class AckMessageHandler extends AbstractMessageHandler {

    public AckMessageHandler(GameChannelService gameService){
        super(gameService);
    }

    @Override
    public int type() {
        return ACK;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        DataBuffer buffer = new DataBuffer(pendingInboundMessage.payload());
        int sid = pendingInboundMessage.sessionId();
        var sz = buffer.getInt();
        for (int i = 0; i < sz; i++)
        {
            gameChannelService.gameChannel(pendingInboundMessage.connectionId()).ack(sid,buffer.getInt());
        }
    }
}
