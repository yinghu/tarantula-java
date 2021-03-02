package com.tarantula.platform.tournament;

import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.tarantula.platform.presence.PresencePortableRegistry;

/**
 * created by yinghu on 3/1/2021.
 */
public class TournamentInstanceQuery implements RecoverableFactory<TournamentInstance> {

    String tournamentId;

    public TournamentInstanceQuery(String tournamentId){
        this.tournamentId = tournamentId;
    }

    public TournamentInstance create() {
        TournamentInstance ocx = new TournamentInstance();
        return ocx;
    }

    public String distributionKey() {
        return this.tournamentId;
    }


    public  int registryId(){
        return PresencePortableRegistry.TOURNAMENT_INSTANCE_CID;
    }

    public String label(){
        return Tournament.INSTANCE_LABEL;
    }
}
