package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.SnowflakeKey;


public class TournamentInstanceQuery implements RecoverableFactory<TournamentInstance> {

    private long tournamentId;
    private String label;

    public TournamentInstanceQuery(long tournamentId,String label){
        this.tournamentId = tournamentId;
        this.label = label;
    }

    public TournamentInstance create() {
        TournamentInstance ocx = new TournamentInstance();
        return ocx;
    }



    public String label(){
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(tournamentId);
    }
}
