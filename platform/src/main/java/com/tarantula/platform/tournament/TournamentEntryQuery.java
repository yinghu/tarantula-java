package com.tarantula.platform.tournament;

import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.tarantula.platform.presence.PresencePortableRegistry;

/**
 * created by yinghu on 3/1/2021.
 */
public class TournamentEntryQuery implements RecoverableFactory<TournamentEntry> {

    String instanceId;

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
        return PresencePortableRegistry.TOURNAMENT_ENTRY_CID;
    }

    public String label(){
        return Tournament.ENTRY_LABEL;
    }
}
