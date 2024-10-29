package com.perfectday.games.earth8;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;
import com.perfectday.games.earth8.inbox.PlayerAction;

public class BannedPlayerQuery implements RecoverableFactory<BannedPlayer> {
    private long systemId;
    public BannedPlayerQuery(long systemId){
        this.systemId = systemId;
    }
    @Override
    public BannedPlayer create() {
        return new BannedPlayer();
    }

    @Override
    public String label() {
        return BannedPlayer.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
