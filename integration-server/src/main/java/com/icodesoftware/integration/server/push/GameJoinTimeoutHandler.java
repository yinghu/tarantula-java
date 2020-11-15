package com.icodesoftware.integration.server.push;

import com.icodesoftware.integration.AbstractMessageHandler;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 10/7/2020.
 */
public class GameJoinTimeoutHandler extends AbstractMessageHandler {

    public GameJoinTimeoutHandler(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public int type() {
        return GAME_SPEC;
    }

    @Override
    public void onMessage(InboundMessage pendingInboundMessage) {

    }
}
