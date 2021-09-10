package com.tarantula.game.service;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.game.GameEntry;
import com.tarantula.game.GamePortableRegistry;

public class GameEntryQuery implements RecoverableFactory<GameEntry> {

    private String roomId;

    public GameEntryQuery(String roomId){
        this.roomId = roomId;
    }

    public GameEntry create() {
        return new GameEntry();
    }

    public String distributionKey() {
        return this.roomId;
    }

    public  int registryId(){
        return GamePortableRegistry.GAME_ENTRY_CID;
    }

    public String label(){
        return GameEntry.LABEL;
    }
}
