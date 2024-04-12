package com.tarantula.platform.presence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;
import com.perfectday.games.earth8.data.PlayerDataTrack;

public class ProfileQuery implements RecoverableFactory<Profile> {

    private long systemId;

    public ProfileQuery(long systemId){
        this.systemId = systemId;
    }

    @Override
    public Profile create() {
        return new Profile();
    }

    @Override
    public String label() {
        return Profile.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
