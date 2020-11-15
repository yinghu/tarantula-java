package com.icodesoftware.integration.game;

import com.icodesoftware.integration.Game;
import com.icodesoftware.integration.GameChannelService;


/**
 * Created by yinghu lu on 11/15/2020.
 */
abstract public class AbstractGame implements Game {

    protected String zoneId;
    protected String roomId;
    protected GameChannelService gameChannelService;

    public AbstractGame(String zoneId,String roomId,GameChannelService gameChannelService){
        this.zoneId = zoneId;
        this.roomId = roomId;
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

}
