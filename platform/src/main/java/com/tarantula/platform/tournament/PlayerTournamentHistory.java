package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

import com.tarantula.platform.FIFOIndexSet;


public class PlayerTournamentHistory extends FIFOIndexSet {

    public PlayerTournamentHistory(){
        this.label = Tournament.HISTORY_LABEL;
    }
    public PlayerTournamentHistory(int maxHistoryRecords){
        super(Tournament.HISTORY_LABEL,maxHistoryRecords);
    }

    @Override
    public int getFactoryId() {
        return TournamentPortableRegistry.OID;
    }


    @Override
    public int getClassId() {
        return TournamentPortableRegistry.PLAYER_TOURNAMENT_HISTORY_CID;
    }

}
