package com.tarantula.platform.inbox;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;
import com.perfectday.games.earth8.inbox.PlayerAction;

public class GlobalItemGrantEventQuery implements RecoverableFactory<GlobalItemGrantEvent> {
    private long gameclusterID;

    public GlobalItemGrantEventQuery(long gameclusterID){
        this.gameclusterID = gameclusterID;
    }
    @Override
    public GlobalItemGrantEvent create() {
        return new GlobalItemGrantEvent();
    }

    @Override
    public String label() {
        return GlobalItemGrantEvent.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(gameclusterID);
    }
}
