package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class LoadMessageHandler extends AbstractMessageHandler {

    public LoadMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return LOAD;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        this.gameChannelService.gameChannel(pendingInboundMessage.connectionId()).onGame().onLoad(pendingInboundMessage);
    }
}
