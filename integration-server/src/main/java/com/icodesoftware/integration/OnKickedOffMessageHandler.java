package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class OnKickedOffMessageHandler extends AbstractMessageHandler {

    public OnKickedOffMessageHandler(GameChannelService gameService){
        super(gameService);
    }

    @Override
    public int type() {
        return ON_KICKED_OFF;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {

    }
}
