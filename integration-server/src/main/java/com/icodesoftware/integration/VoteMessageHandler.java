package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class VoteMessageHandler extends AbstractMessageHandler {

    public VoteMessageHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return VOTE;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {

    }
}
