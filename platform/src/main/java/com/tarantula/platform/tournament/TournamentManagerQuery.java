package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;


public class TournamentManagerQuery implements RecoverableFactory<TournamentManager> {

    private long nodeId;
    private String label;

    public TournamentManagerQuery(long nodeId, String label){
        this.nodeId = nodeId;
        this.label = label;
    }

    public TournamentManager create() {
        TournamentManager ocx = new TournamentManager();
        return ocx;
    }



    public String label(){
        return label;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(nodeId);
    }
}
