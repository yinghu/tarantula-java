package com.tarantula.platform.room;

import com.tarantula.game.GameZone;

public class GameZoneIndex {

    public GameZone gameZone;
    public int partitionId;
    public boolean localManaged;

    public GameZoneIndex(int partitionId,boolean localManaged){
        this.partitionId = partitionId;
        this.localManaged = localManaged;
    }

    @Override
    public String toString(){
        return "Zone->"+gameZone.distributionKey()+">>"+localManaged+">>>"+partitionId;
    }
}
