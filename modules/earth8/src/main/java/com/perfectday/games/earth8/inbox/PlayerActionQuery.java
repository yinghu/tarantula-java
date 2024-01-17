package com.perfectday.games.earth8.inbox;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class PlayerActionQuery implements RecoverableFactory<PlayerAction> {

    private long systemId;
    public PlayerActionQuery(long systemId){
        this.systemId = systemId;
    }
    @Override
    public PlayerAction create() {
        return new PlayerAction();
    }

    @Override
    public String label() {
        return PlayerAction.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
