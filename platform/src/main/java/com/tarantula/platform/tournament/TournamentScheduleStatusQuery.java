package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.SnowflakeKey;


public class TournamentScheduleStatusQuery implements RecoverableFactory<TournamentScheduleStatus> {

    private long bucketId;


    public TournamentScheduleStatusQuery(long bucketId){
        this.bucketId = bucketId;

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
        return new SnowflakeKey(bucketId);
    }
}
