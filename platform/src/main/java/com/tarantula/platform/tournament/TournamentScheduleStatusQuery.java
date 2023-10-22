package com.tarantula.platform.tournament;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;


public class TournamentScheduleStatusQuery implements RecoverableFactory<TournamentScheduleStatus> {

    private long nodeId;
    private String label;

    public TournamentScheduleStatusQuery(long nodeId, String label){
        this.nodeId = nodeId;
        this.label = label;
    }

    public TournamentScheduleStatus create() {
        TournamentScheduleStatus ocx = new TournamentScheduleStatus();
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
