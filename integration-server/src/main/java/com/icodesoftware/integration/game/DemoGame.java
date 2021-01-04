package com.icodesoftware.integration.game;

import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;

public class DemoGame extends AbstractGame{

    public DemoGame(GameChannelService gameChannelService, GameChannel gameChannel, int maxSessionsPerChannel){
        super(gameChannelService,gameChannel,maxSessionsPerChannel);
    }
    @Override
    public void onAction(InboundMessage inboundMessage) {

    }
    @Override
    public void onLoad(InboundMessage inboundMessage){
        log.warn("load->"+inboundMessage.sessionId());
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.type(MessageHandler.LOAD);
        outboundMessage.sequence(inboundMessage.sequence());
        outboundMessage.ack(true);
        outboundMessage.sessionId(inboundMessage.sessionId());
        int mid = gameChannelService.messageId();
        outboundMessage.messageId(mid);
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.putByte(started?(byte)1:0);
        outboundMessage.payload(dataBuffer.toArray());
        gameChannel.relay(mid,true,null,outboundMessage);
    }
    @Override
    public boolean onMove(InboundMessage inboundMessage){
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(inboundMessage.ack());
        pendingOutboundMessage.messageId(inboundMessage.messageId());
        pendingOutboundMessage.sessionId(inboundMessage.sessionId());
        pendingOutboundMessage.type(inboundMessage.type());
        pendingOutboundMessage.sequence(inboundMessage.sequence());
        pendingOutboundMessage.payload(inboundMessage.payload());
        gameChannel.relay(inboundMessage.sessionId(),inboundMessage.messageId(), inboundMessage.ack(), null, pendingOutboundMessage);
        return false;
    }
    @Override
    public boolean onSpawn(InboundMessage inboundMessage){
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(inboundMessage.ack());
        pendingOutboundMessage.messageId(inboundMessage.messageId());
        pendingOutboundMessage.sessionId(inboundMessage.sessionId());
        pendingOutboundMessage.type(inboundMessage.type());
        pendingOutboundMessage.sequence(inboundMessage.sequence());
        pendingOutboundMessage.payload(inboundMessage.payload());
        gameChannel.relay(inboundMessage.sessionId(),inboundMessage.messageId(),true,null,pendingOutboundMessage);
        return false;
    }
    public boolean onSync(InboundMessage inboundMessage){
        OutboundMessage pendingOutboundMessage = new OutboundMessage();
        pendingOutboundMessage.ack(inboundMessage.ack());
        int mid = this.gameChannelService.messageId();
        pendingOutboundMessage.messageId(mid);
        pendingOutboundMessage.sessionId(inboundMessage.sessionId());
        pendingOutboundMessage.type(inboundMessage.type());
        pendingOutboundMessage.sequence(inboundMessage.sequence());
        pendingOutboundMessage.payload(inboundMessage.payload());
        gameChannel.relay(inboundMessage.sessionId(),mid,inboundMessage.ack(),null,pendingOutboundMessage);
        return false;
    }
    public boolean onCollision(InboundMessage inboundMessage){
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.ack(inboundMessage.ack());
        outboundMessage.type(MessageHandler.ON_COLLISION);
        outboundMessage.sequence(inboundMessage.sequence());
        int mid = gameChannelService.messageId();
        outboundMessage.messageId(mid);
        gameChannel.relay(inboundMessage.sessionId(),mid,inboundMessage.ack(),null,outboundMessage);
        return false;
    }
    public boolean onDestroy(InboundMessage inboundMessage){
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.ack(inboundMessage.ack());
        outboundMessage.type(inboundMessage.type());
        outboundMessage.sequence(inboundMessage.sequence());
        int mid = gameChannelService.messageId();
        outboundMessage.messageId(mid);
        gameChannel.relay(inboundMessage.sessionId(),mid,inboundMessage.ack(),null,outboundMessage);
        return false;
    }
}
