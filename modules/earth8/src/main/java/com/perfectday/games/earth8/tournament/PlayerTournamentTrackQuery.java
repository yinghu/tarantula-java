package com.perfectday.games.earth8.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;
import com.perfectday.games.earth8.inbox.PlayerAction;

public class PlayerTournamentTrackQuery implements RecoverableFactory<PlayerTournamentTrack> {
    private long systemId;
    public PlayerTournamentTrackQuery(long systemId){
        this.systemId = systemId;
    }
    @Override
    public PlayerTournamentTrack create() {
        return new PlayerTournamentTrack();
    }

    @Override
    public String label() {
        return PlayerTournamentTrack.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
