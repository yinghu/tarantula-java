package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.PendingInboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class LeaveMessageHandler implements MessageHandler {
    private final GameChannelService gameChannelService;
    public LeaveMessageHandler(GameChannelService gameService){
        this.gameChannelService = gameService;
    }

    @Override
    public int type() {
        return LEAVE;
    }

    @Override
    public void onMessage(PendingInboundMessage pendingInboundMessage) {

    }
}
