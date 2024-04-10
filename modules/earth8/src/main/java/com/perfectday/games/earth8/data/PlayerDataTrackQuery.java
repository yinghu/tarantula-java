package com.perfectday.games.earth8.data;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class PlayerDataTrackQuery implements RecoverableFactory<PlayerDataTrack> {
    private long systemId;
    public PlayerDataTrackQuery(long systemId){
        this.systemId = systemId;
    }
    @Override
    public PlayerDataTrack create() {
        return new PlayerDataTrack();
    }

    @Override
    public String label() {
        return PlayerDataTrack.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
