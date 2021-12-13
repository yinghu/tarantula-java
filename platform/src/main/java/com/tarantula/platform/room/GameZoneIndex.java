package com.tarantula.platform.room;

import com.tarantula.game.GameZone;

public class GameZoneIndex {

    public GameZone gameZone;
    public boolean localManaged;

    public GameZoneIndex(GameZone gameZone,boolean localManaged){
        this.gameZone = gameZone;
        this.localManaged = localManaged;
    }

    @Override
    public String toString(){
        return "Zone->"+gameZone.distributionKey()+">>"+localManaged;
    }
}
