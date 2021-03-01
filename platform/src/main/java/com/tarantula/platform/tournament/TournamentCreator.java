package com.tarantula.platform.tournament;

import com.icodesoftware.Tournament;

public class TournamentCreator implements Tournament.Creator {

    @Override
    public Tournament tournament(String type, Tournament.Schedule schedule) {
        return new DefaultTournament(type,schedule,this);
    }

    @Override
    public Tournament tournament() {
        return new DefaultTournament();
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
