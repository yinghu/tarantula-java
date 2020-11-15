package com.icodesoftware.integration.server.push;

import com.icodesoftware.integration.AbstractMessageHandler;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class GameSpecHandler extends AbstractMessageHandler {

    public GameSpecHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return GAME_SPEC;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        DataBuffer dataBuffer = new DataBuffer(pendingInboundMessage.payload());
        int level = dataBuffer.getInt();
        int capacity = dataBuffer.getInt();
        long dur = dataBuffer.getLong();
        long ovt = dataBuffer.getLong();
        String name = dataBuffer.getUTF8();
        String zoneId = dataBuffer.getUTF8();
        long connectionId = dataBuffer.getLong();
        System.out.println(zoneId+"///"+connectionId);
    }
}
