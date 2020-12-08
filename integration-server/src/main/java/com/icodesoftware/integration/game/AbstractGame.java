package com.icodesoftware.integration.game;

import com.icodesoftware.integration.Game;
import com.icodesoftware.integration.GameChannel;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.DataBuffer;


/**
 * Created by yinghu lu on 11/15/2020.
 */
abstract public class AbstractGame implements Game {

    protected String zoneId;
    protected String roomId;
    protected GameChannelService gameChannelService;
    protected GameChannel gameChannel;

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


    public void onSpec(DataBuffer dataBuffer){
        int level = dataBuffer.getInt();
        int capacity = dataBuffer.getInt();
        long dur = dataBuffer.getLong();
        long ovt = dataBuffer.getLong();
        roomId = dataBuffer.getUTF8();
        zoneId = dataBuffer.getUTF8();
    }
    public void onStart(){
        this.gameChannelService.onUpdate(this,"onStart","{}".getBytes());
    }
    public void onClosing(){
        this.gameChannelService.onUpdate(this,"onClosing","{}".getBytes());
    }
    public void onClose(){
        this.gameChannelService.onUpdate(this,"onClose","{}".getBytes());
    }
    public void onEnd(){
        this.gameChannelService.onUpdate(this,"onEnd","{}".getBytes());
    }
    public void onOvertime(){
        this.gameChannelService.onUpdate(this,"onOvertime","{}".getBytes());
    }
    public void onJoinTimeout(){
        this.gameChannelService.onUpdate(this,"onJoinTimeout","{}".getBytes());
    }
}
