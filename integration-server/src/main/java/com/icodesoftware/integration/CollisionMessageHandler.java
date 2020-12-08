package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class CollisionMessageHandler extends AbstractMessageHandler {

    public CollisionMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return COLLISION;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        System.out.println("hit->");
    }
}
