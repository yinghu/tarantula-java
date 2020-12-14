package com.icodesoftware.integration.game;

import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public class DynamicBattle extends AbstractGame {


    public DynamicBattle(GameChannelService gameChannelService, GameChannel gameChannel,int maxSessionsPerChannel){
        super(gameChannelService,gameChannel,maxSessionsPerChannel);
    }
    public void onAction(InboundMessage inboundMessage){
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.type(MessageHandler.ON_ACTION);
        outboundMessage.sequence(inboundMessage.sequence());
        outboundMessage.ack(inboundMessage.ack());
        int mid = gameChannelService.messageId();
        outboundMessage.messageId(gameChannelService.messageId());
        gameChannel.relay(inboundMessage.sessionId(),mid,true,null,outboundMessage);
    }

}
