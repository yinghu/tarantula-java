package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class VoteMessageHandler implements MessageHandler {
    private final GameChannelService gameChannelService;
    public VoteMessageHandler(GameChannelService gameService){
        this.gameChannelService = gameService;
    }

    @Override
    public int type() {
        return VOTE;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {

    }
}
