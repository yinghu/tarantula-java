package com.tarantula.platform.presence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class PlatformBannedPlayerQuery implements RecoverableFactory<PlatformBannedPlayer> {

    private long systemId;

    public PlatformBannedPlayerQuery(long systemId){
        this.systemId = systemId;
    }
    @Override
    public PlatformBannedPlayer create() {
        return new PlatformBannedPlayer();
    }

    @Override
    public String label() {
        return PlatformBannedPlayer.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
