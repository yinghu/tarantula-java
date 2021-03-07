package com.tarantula.platform.tournament;

import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.Tournament;
import com.tarantula.platform.presence.PresencePortableRegistry;

/**
 * created by yinghu on 3/1/2021.
 */
public class TournamentEntryQuery implements RecoverableFactory<TournamentEntry> {

    private String instanceId;
    private Tournament.Listener listener;

    public TournamentEntryQuery(String instanceId, Tournament.Listener listener){
        this.instanceId = instanceId;
        this.listener = listener;
    }

    public TournamentEntry create() {
        TournamentEntry ocx = new TournamentEntry(listener);
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
