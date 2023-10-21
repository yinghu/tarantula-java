package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.SnowflakeKey;


public class TournamentEntryQuery implements RecoverableFactory<TournamentEntry> {

    private long instanceId;

    public TournamentEntryQuery(long instanceId){
        this.instanceId = instanceId;
    }

    public TournamentEntry create() {
        TournamentEntry ocx = new TournamentEntry();
        return ocx;
    }



    public String label(){
        return Tournament.ENTRY_LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(instanceId);
    }
}
