package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.SnowflakeKey;


public class TournamentHistoryQuery implements RecoverableFactory<TournamentHistory> {

    private long systemId;
    public TournamentHistoryQuery(long systemId){
        this.systemId = systemId;
    }

    public TournamentHistory create() {
        return new TournamentHistory();
    }



    public String label(){
        return Tournament.HISTORY_LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return SnowflakeKey.from(systemId);
    }
}
