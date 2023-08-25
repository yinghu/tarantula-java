package com.tarantula.platform.room;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.LongTypeKey;
import com.tarantula.game.GamePortableRegistry;

public class GameEntryQuery implements RecoverableFactory<GameEntry> {

    private String roomId;

    private long roomKey;
    public GameEntryQuery(String roomId){
        this.roomId = roomId;
    }
    public GameEntryQuery(long roomId){
        this.roomKey = roomId;
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

    @Override
    public Recoverable.Key key() {
        return new LongTypeKey(roomKey);
    }
}
