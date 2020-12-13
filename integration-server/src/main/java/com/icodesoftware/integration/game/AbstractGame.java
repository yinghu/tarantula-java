package com.icodesoftware.integration.game;

import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.integration.Game;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.integration.channel.RemoteSession;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.protocol.*;

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
    protected GameObject gameObject;
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
        gameObject.update(remoteSession.seat).rank=1;
        gameObject.update(remoteSession.seat).xp=0;
        gameObject.update(new GameStatsDelta(remoteSession.seat,"kills",1));
        gameObject.update(new GameStatsDelta(remoteSession.seat,"winnings",1));
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
            gameChannelService.onUpdate(this,"onStats",gameObject.toJson().toString().getBytes());
        });
    }
    public boolean onSpawn(InboundMessage inboundMessage){
        DataBuffer dataBuffer = new DataBuffer(inboundMessage.payload());
        gameObject.gameItem(new GameItem(dataBuffer.getInt(),dataBuffer.getInt(),inboundMessage.sessionId()));
        return true;
    }
    public boolean onCollision(InboundMessage inboundMessage){
        GameItem gameItem = gameObject.gameItem(inboundMessage.sequence());
        gameItem.collisions.incrementAndGet();
        gameChannel.onSession(inboundMessage.sessionId(),(session)->{
            gameObject.update(session.seat()).xp +=100;
            gameObject.update(new GameStatsDelta(session.seat(),"hits",1));
            gameChannelService.onUpdate(this,"onStats",gameObject.toJson().toString().getBytes());
        });
        return true;
    }
    public boolean onDestroy(InboundMessage inboundMessage){
        return true;
    }
    public boolean onMove(InboundMessage inboundMessage){
        return true;
    }
    public boolean onSync(InboundMessage inboundMessage){
        return true;
    }


    public void onSpec(DataBuffer dataBuffer){
        int level = dataBuffer.getInt();
        int capacity = dataBuffer.getInt();
        long dur = dataBuffer.getLong();
        long ovt = dataBuffer.getLong();
        roomId = dataBuffer.getUTF8();
        zoneId = dataBuffer.getUTF8();
        this.gameObject = new GameObject(capacity);
    }
    public void onStart(){
        this.started = true;
    }
    public void onClosing(){

    }
    public void onClose(){
        gameChannelService.onUpdate(this,"onClose",gameObject.toJson().toString().getBytes());
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
