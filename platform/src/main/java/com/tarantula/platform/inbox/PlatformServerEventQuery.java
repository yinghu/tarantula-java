package com.tarantula.platform.inbox;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class PlatformServerEventQuery implements RecoverableFactory<PlatformServerEvent> {
    private long playerID;

    public PlatformServerEventQuery(long playerID){
        this.playerID = playerID;
    }
    @Override
    public PlatformServerEvent create() {
        return new PlatformServerEvent();
    }

    @Override
    public String label() {
        return PlatformServerEvent.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(playerID);
    }
}
