package com.icodesoftware.integration.game;

import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.InboundMessage;

public class DemoGame extends AbstractGame{

    public DemoGame(GameChannelService gameChannelService, GameChannel gameChannel, int maxSessionsPerChannel){
        super(gameChannelService,gameChannel,maxSessionsPerChannel);
    }
    @Override
    public void onAction(InboundMessage inboundMessage) {

    }
}
