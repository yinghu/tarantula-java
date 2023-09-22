package com.tarantula.platform.room;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.game.GameZone;

public class GameRoomQuery implements RecoverableFactory<GameRoom> {

    private long ownerId;
    private String roomType;
    private int capacity;

    public GameRoomQuery(long ownerId){
        this.ownerId = ownerId;
        this.roomType = GameZone.PLAY_MODE_PVE;
        this.capacity = 1;
    }
    public GameRoomQuery(long ownerId,String roomType,int capacity){
        this.ownerId = ownerId;
        this.roomType = roomType;
        this.capacity = capacity;
    }

    public GameRoom create() {
        return GameRoom.newGameRoom(roomType,capacity);
    }


    public String label(){
        return GameRoom.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(ownerId);
    }
}
