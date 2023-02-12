package com.tarantula.platform.tournament;

import com.icodesoftware.service.ClusterProvider;
import com.tarantula.platform.IndexSet;

public class TournamentHeaderIndex {

    public TournamentHeader tournamentHeader;
    public IndexSet instanceIndex;

    public ClusterProvider.ClusterStore instanceStore;


    @Override
    public String toString(){
        return "Tournament->"+tournamentHeader.distributionKey();
    }
}
