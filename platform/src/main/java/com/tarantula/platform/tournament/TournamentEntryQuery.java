package com.tarantula.platform.tournament;

import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;


public class TournamentEntryQuery implements RecoverableFactory<TournamentEntry> {

    private String instanceId;

    public TournamentEntryQuery(String instanceId){
        this.instanceId = instanceId;
    }

    public TournamentEntry create() {
        TournamentEntry ocx = new TournamentEntry();
        return ocx;
    }

    public String distributionKey() {
        return this.instanceId;
    }


    public  int registryId(){
        return TournamentPortableRegistry.TOURNAMENT_ENTRY_CID;
    }

    public String label(){
        return Tournament.ENTRY_LABEL;
    }
}
