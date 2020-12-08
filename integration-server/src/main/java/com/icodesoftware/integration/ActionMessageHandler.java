package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class ActionMessageHandler extends AbstractMessageHandler {

    public ActionMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return ACTION;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        this.gameChannelService.gameChannel(pendingInboundMessage.connectionId()).onGame().onAction(pendingInboundMessage);
    }
}
