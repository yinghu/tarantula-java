package com.icodesoftware.integration;

import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class PongMessageHandler extends AbstractMessageHandler {

    public PongMessageHandler(GameChannelService gameService){
        super(gameService);
    }

    @Override
    public int type() {
        return PONG;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {
        GameChannel gameChannel = gameChannelService.gameChannel(pendingInboundMessage.connectionId());
        if(gameChannel!=null){
            gameChannel.pong(pendingInboundMessage.sessionId());
        }
    }
}
