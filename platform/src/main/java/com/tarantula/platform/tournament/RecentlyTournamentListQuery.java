package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;

public class RecentlyTournamentListQuery implements RecoverableFactory<RecentlyTournamentList> {

    private long gameClusterId;

    public RecentlyTournamentListQuery(long gameClusterId){
        this.gameClusterId = gameClusterId;
    }

    @Override
    public RecentlyTournamentList create() {
        return new RecentlyTournamentList();
    }

    @Override
    public String label() {
        return RecentlyTournamentList.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(gameClusterId);
    }
}
