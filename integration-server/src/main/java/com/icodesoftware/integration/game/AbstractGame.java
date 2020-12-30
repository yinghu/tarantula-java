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
        log.warn("join->"+remoteSession.seat+"/"+sessionId);
        //gameObject.update(remoteSession.seat).rank=1;
        //gameObject.update(remoteSession.seat).xp=0;
        //gameObject.update(new GameStatsDelta(remoteSession.seat,"kills",1));
        //gameObject.update(new GameStatsDelta(remoteSession.seat,"winnings",1));
    }
    public void onLeave(int sessionId){
        log.warn("leave->"+sessionId);
    }
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
        gameChannel.onSession(inboundMessage.sessionId(),(session)->{
            gameChannelService.onUpdate(this,"onStats",gameObject.toJson().toString().getBytes());
        });
    }
    public boolean onSpawn(InboundMessage inboundMessage){
        log.warn("on spawn->");
        DataBuffer dataBuffer = new DataBuffer(inboundMessage.payload());
        gameObject.gameItem(new GameItem(dataBuffer.getInt(),dataBuffer.getInt(),inboundMessage.sessionId()));
        return true;
    }
    public boolean onCollision(InboundMessage inboundMessage){
        GameItem gameItem = gameObject.gameItem(inboundMessage.sequence());
        if(gameItem==null){
            return false;
        }
        gameItem.collisions.incrementAndGet();
        gameChannel.onSession(inboundMessage.sessionId(),(session)->{
            gameObject.update(session.seat()).xp +=100;
            gameObject.update(new GameStatsDelta(session.seat(),"hits",1));
            gameChannelService.onUpdate(this,"onStats",gameObject.toJson().toString().getBytes());
        });
        return true;
    }
    public boolean onDestroy(InboundMessage inboundMessage){
        GameItem gameItem = gameObject.items().remove(inboundMessage.sequence());
        if(gameItem==null){
            return false;
        }
        OutboundMessage outboundMessage = new OutboundMessage();
        outboundMessage.type(MessageHandler.SPAWN);
        outboundMessage.ack(true);
        outboundMessage.sequence(1);
        int mid = gameChannelService.messageId();
        outboundMessage.messageId(mid);
        DataBuffer dataBuffer = new DataBuffer();
        dataBuffer.putInt(gameItem.typeId);
        int seq = gameChannelService.messageId();
        dataBuffer.putInt(seq);
        outboundMessage.payload(dataBuffer.toArray());
        gameObject.gameItem(new GameItem(gameItem.typeId,seq,0));
        gameChannel.relay(mid,true,null,outboundMessage);
        return true;
    }
    public boolean onMove(InboundMessage inboundMessage){
        //DataBuffer dataBuffer = new DataBuffer(inboundMessage.payload());
        //Vector3 _pos = dataBuffer.getVector3();
        //log.warn(_pos.toString());
        return true;
    }
    public boolean onSync(InboundMessage inboundMessage){
        log.warn("on sync->"+inboundMessage.sequence()+"//"+inboundMessage.sessionId());
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
    public void onClosing(){
        log.warn("closing");
    }
    public void onClose(){
        gameChannelService.onUpdate(this,"onClose",gameObject.toJson().toString().getBytes());
        this.listener.onChannelClosed(gameChannel);
    }
    public void onOvertime(){
        log.warn("overtime");
    }
    public void onJoinTimeout(){
        log.warn("join timeout");
        //do kick out
        this.listener.onChannelReset(gameChannel);
    }
    public void onGameLoop(){
        gameObject.items().forEach((s,g)->{
            if(g.typeId==4){
                OutboundMessage outboundMessage = new OutboundMessage();
                outboundMessage.type(MessageHandler.MOVE);
                outboundMessage.ack(true);
                outboundMessage.sequence(g.sequence);
                int mid = gameChannelService.messageId();
                outboundMessage.messageId(mid);
                DataBuffer dataBuffer = new DataBuffer();
                Vector3 next = new Vector3(g.lastPosition.x+2,0,0);
                g.lastPosition = next;
                dataBuffer.putVector3(next);
                dataBuffer.putFloat(6.0f);
                outboundMessage.payload(dataBuffer.toArray());
                gameChannel.relay(mid,true,null,outboundMessage);
            }
        });
    }
    public void registerGameChannelListener(GameChannel.Listener listener){
        this.listener = listener;
    }
}
