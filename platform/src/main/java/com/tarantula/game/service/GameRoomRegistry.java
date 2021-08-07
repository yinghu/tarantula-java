package com.tarantula.game.service;


import com.tarantula.game.GamePortableRegistry;
import com.tarantula.platform.RoomRegistry;


public class GameRoomRegistry extends RoomRegistry {

    public GameRoomRegistry(){
        super();
    }
    public GameRoomRegistry(int maxSize){
        super(maxSize);
    }
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    public int getClassId() {
        return GamePortableRegistry.ROOM_REGISTRY_CID;
    }

}
