package com.icodesoftware.integration.game;

import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.Game;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.integration.channel.RemoteSession;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.DataBuffer;
import com.icodesoftware.protocol.InboundMessage;
import com.icodesoftware.protocol.MessageHandler;
import com.icodesoftware.protocol.OutboundMessage;


/**
 * Created by yinghu lu on 11/15/2020.
 */
abstract public class AbstractGame implements Game {
    protected TarantulaLogger log = JDKLogger.getLogger(AbstractGame.class);
    protected String zoneId;
    protected String roomId;
    protected boolean started;
    protected GameChannelService gameChannelService;
    protected GameChannel gameChannel;
    protected GameChannel.Listener listener;
    public AbstractGame(GameChannelService gameChannelService, GameChannel gameChannel){
        this.gameChannelService = gameChannelService;
        this.gameChannel = gameChannel;
    }

    @Override
    public String zoneId() {
        return zoneId;
    }
    @Override
    public String roomId() {
        return roomId;
    }

    public boolean started(){
        return this.started;
    }
    public void onJoin(int sessionId,RemoteSession remoteSession){
        log.warn("join->"+remoteSession.seat+"/"+sessionId);
        //OutboundMessage outboundMessage = new OutboundMessage();
        //outboundMessage.type(MessageHandler.ON_SPAWN);
        //outboundMessage.sequence(1);
        //outboundMessage.ack(true);
        //outboundMessage.sessionId(sessionId);
        //int mid = gameChannelService.messageId();
        //gameChannel.relay(mid,true,null,outboundMessage);
    }
    public void onLeave(int sessionId){
        log.warn("leave->"+sessionId);
    }
    public void onLoad(InboundMessage inboundMessage){
        log.warn("load->"+inboundMessage.sessionId());
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.type(MessageHandler.ON_SPAWN);
        outboundMessage.sequence(inboundMessage.sequence());
        outboundMessage.ack(true);
        outboundMessage.sessionId(inboundMessage.sessionId());
        int mid = gameChannelService.messageId();
        outboundMessage.messageId(mid);
        gameChannel.relay(mid,true,null,outboundMessage);
        gameChannel.onSession(inboundMessage.sessionId(),(session)->{
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("seat",session.seat());
            jsonObject.addProperty("name","kills");
            jsonObject.addProperty("value",6);
            gameChannelService.onUpdate(this,"onStats",jsonObject.toString().getBytes());
        });
    }
    public void onCollision(InboundMessage inboundMessage){
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.ack(inboundMessage.ack());
        outboundMessage.type(MessageHandler.ON_COLLISION);
        outboundMessage.sequence(inboundMessage.sequence());
        int mid = gameChannelService.messageId();
        outboundMessage.messageId(mid);
        gameChannel.relay(mid,inboundMessage.ack(),null,outboundMessage);
    }

    public void onSpec(DataBuffer dataBuffer){
        int level = dataBuffer.getInt();
        int capacity = dataBuffer.getInt();
        long dur = dataBuffer.getLong();
        long ovt = dataBuffer.getLong();
        roomId = dataBuffer.getUTF8();
        zoneId = dataBuffer.getUTF8();
    }
    public void onStart(){
        this.started = true;
    }
    public void onClosing(){

    }
    public void onClose(){
        gameChannel.onSession((session)->{
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("seat",session.seat());
            jsonObject.addProperty("name","kills");
            jsonObject.addProperty("value",6);
            gameChannelService.onUpdate(this,"onClose",jsonObject.toString().getBytes());
        });
    }
    public void onEnd(){
        this.listener.onChannelClosed(gameChannel);
    }
    public void onOvertime(){

    }
    public void onJoinTimeout(){

    }
    public void registerGameChannelListener(GameChannel.Listener listener){
        this.listener = listener;
    }
}
