package com.icodesoftware.integration.server.push;

import com.icodesoftware.integration.AbstractMessageHandler;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class GameCloseHandler extends AbstractMessageHandler {

    public GameCloseHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return GAME_CLOSE;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {

    }
}
