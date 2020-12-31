package com.icodesoftware.integration.game;

import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.*;

/**
 * Created by yinghu lu on 11/15/2020.
 */
public class DynamicBattle extends AbstractGame {


    public DynamicBattle(GameChannelService gameChannelService, GameChannel gameChannel,int maxSessionsPerChannel){
        super(gameChannelService,gameChannel,maxSessionsPerChannel);
    }
    public void onLoad(InboundMessage inboundMessage){
        super.onLoad(inboundMessage);
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.type(MessageHandler.SPAWN);
        outboundMessage.ack(true);
        outboundMessage.sequence(1);
        int mid = gameChannelService.messageId();
        outboundMessage.messageId(mid);
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.putInt(4);
        int seq = gameChannelService.messageId();
        dataBuffer.putInt(seq);
        outboundMessage.payload(dataBuffer.toArray());
        //gameObject.gameItem(new GameItem(4,seq,0));
        //gameChannel.relay(mid,true,null,outboundMessage);
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
