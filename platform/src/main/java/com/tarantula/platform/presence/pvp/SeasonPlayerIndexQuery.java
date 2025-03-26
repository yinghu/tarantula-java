package com.tarantula.platform.presence.pvp;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class SeasonPlayerIndexQuery implements RecoverableFactory<SeasonPlayerIndex> {

    private long seasonId;

    public SeasonPlayerIndexQuery(long seasonId){
        this.seasonId = seasonId;
    }
    @Override
    public SeasonPlayerIndex create() {
        return new SeasonPlayerIndex();
    }

    @Override
    public String label() {
        return SeasonPlayerIndex.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(seasonId);
    }
}
