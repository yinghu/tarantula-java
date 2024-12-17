package com.tarantula.platform.inbox;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class PlatformItemGrantEventQuery implements RecoverableFactory<PlatformItemGrantEvent> {
    private long playerID;

    public PlatformItemGrantEventQuery(long playerID){
        this.playerID = playerID;
    }
    @Override
    public PlatformItemGrantEvent create() {
        return new PlatformItemGrantEvent();
    }

    @Override
    public String label() {
        return PlatformItemGrantEvent.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(playerID);
    }
}
