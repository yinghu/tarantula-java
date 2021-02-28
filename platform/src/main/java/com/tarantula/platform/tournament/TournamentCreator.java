package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

public class TournamentCreator implements Tournament.Creator {

    @Override
    public Tournament tournament(String type) {
        return new DefaultTournament(type,this);
    }

    @Override
    public Tournament.Instance instance() {
        TournamentInstance tournamentInstance = new TournamentInstance();
        return tournamentInstance;
    }

    @Override
    public Tournament.Entry entry(String s) {
        TournamentEntry entry = new TournamentEntry();
        return entry;
    }
}
