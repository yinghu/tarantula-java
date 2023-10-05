package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

import com.icodesoftware.util.RecoverableObject;


public class PlayerTournamentHistory extends RecoverableObject {

    public PlayerTournamentHistory(){
        this.label = Tournament.HISTORY_LABEL;
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
