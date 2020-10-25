package com.icodesoftware.integration;

import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class PongMessageHandler implements MessageHandler {
    private final GameChannelService gameChannelService;
    public PongMessageHandler(GameChannelService gameService){
        this.gameChannelService = gameService;
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
