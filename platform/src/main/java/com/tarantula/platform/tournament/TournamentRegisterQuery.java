package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.SnowflakeKey;


public class TournamentRegisterQuery implements RecoverableFactory<TournamentRegister> {

    private Recoverable.Key tournamentId;

    public TournamentRegisterQuery(Recoverable.Key tournamentId){
        this.tournamentId = tournamentId;
    }

    public TournamentRegister create() {
        return new TournamentRegister();
    }



    public String label(){
        return Tournament.REGISTER_LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return tournamentId;
    }
}
