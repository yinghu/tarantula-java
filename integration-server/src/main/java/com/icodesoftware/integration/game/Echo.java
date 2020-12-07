package com.icodesoftware.integration.game;

import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public class Echo extends AbstractGame {

    public Echo(GameChannelService gameChannelService){
        super(gameChannelService);
    }

    @Override
    public MessageHandler onAction(InboundMessage inboundMessage){
        gameChannelService.onUpdate(this,1,"{}".getBytes());
        return null;
    }
}
