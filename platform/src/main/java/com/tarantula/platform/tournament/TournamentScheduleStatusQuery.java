package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;



public class TournamentScheduleStatusQuery implements RecoverableFactory<TournamentScheduleStatus> {

    private Recoverable.Key key;


    public TournamentScheduleStatusQuery(Recoverable.Key key){
        this.key = key;

    }

    public TournamentScheduleStatus create() {
        TournamentScheduleStatus ocx = new TournamentScheduleStatus();
        return ocx;
    }



    public String label(){
        return Tournament.SCHEDULE_LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
