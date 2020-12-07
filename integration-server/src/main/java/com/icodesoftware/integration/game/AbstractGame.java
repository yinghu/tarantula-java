package com.icodesoftware.integration.game;

import com.icodesoftware.integration.Game;
import com.icodesoftware.integration.GameChannelService;
import com.icodesoftware.protocol.DataBuffer;


/**
 * Created by yinghu lu on 11/15/2020.
 */
abstract public class AbstractGame implements Game {

    protected String zoneId;
    protected String roomId;
    protected GameChannelService gameChannelService;

    public AbstractGame(GameChannelService gameChannelService){
        this.gameChannelService = gameChannelService;
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
    public void onStart(){}
    public void onClosing(){}
    public void onClose(){}
    public void onEnd(){}
    public void onOvertime(){}
    public void onJoinTimeout(){}
}
