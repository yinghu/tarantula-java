package com.tarantula.platform.room;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.game.GamePortableRegistry;

public class GameRoomRegistryQuery implements RecoverableFactory<GameRoomRegistry> {

    private String roomId;

    public GameRoomRegistryQuery(String roomId){
        this.roomId = roomId;
    }

    public GameRoomRegistry create() {
        return new GameRoomRegistry();
    }

    public String distributionKey() {
        return this.roomId;
    }

    public  int registryId(){
        return GamePortableRegistry.GAME_ROOM_REGISTRY_CID;
    }

    public String label(){
        return GameRoomRegistry.LABEL;
    }
}
