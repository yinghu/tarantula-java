package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.SnowflakeKey;


public class TournamentManagerQuery implements RecoverableFactory<TournamentManager> {

    private long bucketId;

    public TournamentManagerQuery(long bucketId){
        this.bucketId = bucketId;

    }

    public TournamentManager create() {
        TournamentManager ocx = new TournamentManager();
        return ocx;
    }



    public String label(){
        return Tournament.MANAGER_LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(bucketId);
    }
}
