package com.icodesoftware.integration.game;

import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.InboundMessage;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public class Echo extends AbstractGame {

    public Echo(String zoneId,String roomId,GameChannelService gameChannelService){
        super(zoneId,roomId,gameChannelService);
    }

    @Override
    public void onMessage(InboundMessage inboundMessage){
        gameChannelService.onUpdate(this,"{}".getBytes());
    }
}
