package com.tarantula.platform.room;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.OidKey;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.game.GamePortableRegistry;

public class GameEntryQuery implements RecoverableFactory<GameEntry> {

    private long roomId;


    public GameEntryQuery(long roomId){
        this.roomId = roomId;
    }

    public GameEntry create() {
        return new GameEntry();
    }


    public  int registryId(){
        return GamePortableRegistry.GAME_ENTRY_CID;
    }

    public String label(){
        return GameEntry.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(roomId);
    }
}
