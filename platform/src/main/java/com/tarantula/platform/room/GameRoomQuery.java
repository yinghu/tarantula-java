package com.tarantula.platform.room;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class GameRoomQuery implements RecoverableFactory<GameRoomHeader> {

    private long zoneId;

    public GameRoomQuery(long zoneId){
        this.zoneId = zoneId;

    }

    public GameRoomHeader create() {
        return new GameRoomHeader();
    }


    public String label(){
        return GameRoom.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(zoneId);
    }
}
