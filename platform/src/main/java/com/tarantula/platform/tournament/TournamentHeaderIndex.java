package com.tarantula.platform.tournament;

import com.icodesoftware.service.ClusterProvider;

public class TournamentHeaderIndex {

    public TournamentHeader tournamentHeader;

    public ClusterProvider.ClusterStore instanceStore;


    @Override
    public String toString(){
        return "Tournament->"+tournamentHeader.distributionKey();
    }
}
