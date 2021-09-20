package com.tarantula.platform.tournament;

import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;


public class TournamentPrizeQuery implements RecoverableFactory<TournamentPrize> {

    private String instanceId;

    public TournamentPrizeQuery(String instanceId){
        this.instanceId = instanceId;
    }

    public TournamentPrize create() {
        TournamentPrize ocx = new TournamentPrize();
        return ocx;
    }

    public String distributionKey() {
        return this.instanceId;
    }


    public  int registryId(){
        return TournamentPortableRegistry.TOURNAMENT_PRIZE_CID;
    }

    public String label(){
        return Tournament.PRIZE_LABEL;
    }
}
