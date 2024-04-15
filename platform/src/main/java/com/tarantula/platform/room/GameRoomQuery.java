package com.tarantula.platform.room;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class GameRoomQuery implements RecoverableFactory<GameRoomHeader> {

    private long ownerId;

    public GameRoomQuery(long ownerId){
        this.ownerId = ownerId;

    }

    public GameRoomHeader create() {
        return new GameRoomHeader();
    }


    public String label(){
        return GameRoom.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(ownerId);
    }
}
