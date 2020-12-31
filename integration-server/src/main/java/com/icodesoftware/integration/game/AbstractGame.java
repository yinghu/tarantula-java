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
    protected int capacity;
    protected int level;
    protected boolean started;
    protected GameChannelService gameChannelService;
    protected GameChannel gameChannel;
    protected GameChannel.Listener listener;
    protected GameObject gameObject;
    public AbstractGame(GameChannelService gameChannelService, GameChannel gameChannel,int maxSessionsPerChannel){
        this.gameChannelService = gameChannelService;
        this.gameChannel = gameChannel;
        this.gameObject = new GameObject(maxSessionsPerChannel);
    }

    @Override
    public String zoneId() {
        return zoneId;
    }
    @Override
    public String roomId() {
        return roomId;
    }

    public void onJoin(int sessionId,RemoteSession remoteSession){

    }
    public void onLeave(int sessionId){

    }
    public void onLoad(InboundMessage inboundMessage){

    }
    public boolean onSpawn(InboundMessage inboundMessage){
        return true;
    }
    public boolean onCollision(InboundMessage inboundMessage){
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
        zoneId = dataBuffer.getUTF8();
        roomId = dataBuffer.getUTF8();
        level = dataBuffer.getInt();
        capacity = dataBuffer.getInt();
    }
    public void onStart(){
        this.started = true;
    }
    public void onClosing(){ }
    public void onClose(){
        gameChannelService.onUpdate(this,"onClose",gameObject.toJson().toString().getBytes());
        this.listener.onChannelClosed(gameChannel);
    }
    public void onOvertime(){ }
    public void onJoinTimeout(){
        this.listener.onChannelReset(gameChannel);
    }
    public void onGameLoop(){ }
    public void registerGameChannelListener(GameChannel.Listener listener){
        this.listener = listener;
    }
}
