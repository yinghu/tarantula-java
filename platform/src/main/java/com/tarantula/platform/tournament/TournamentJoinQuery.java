package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;


public class TournamentJoinQuery implements RecoverableFactory<TournamentJoin> {

    private Recoverable.Key key;
    private String label;

    public TournamentJoinQuery(Recoverable.Key key, String label){
        this.key = key;
        this.label = label;
    }

    public TournamentJoin create() {
        TournamentJoin ocx = new TournamentJoin();
        return ocx;
    }



    public String label(){
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
