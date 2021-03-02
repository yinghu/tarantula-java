package com.tarantula.platform.tournament;

import com.icodesoftware.DataStore;
import com.icodesoftware.Tournament;

import java.time.LocalDateTime;

public class TournamentCreator implements Tournament.Creator {

    private final DataStore dataStore;
    public TournamentCreator(DataStore dataStore){
        this.dataStore = dataStore;
    }

    @Override
    public Tournament create(String type, Tournament.Schedule schedule) {
        Tournament tournament = new DefaultTournament(type,schedule,this);
        this.dataStore.create(tournament);
        return tournament;
    }

    @Override
    public Tournament load(String tournamentId) {
        Tournament tournament = new DefaultTournament();
        tournament.distributionKey(tournamentId);
        tournament.registerCreator(this);
        return dataStore.load(tournament)?tournament:null;
    }

    @Override
    public Tournament.Instance create(Tournament tournament) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime close = start.plusMinutes(tournament.durationMinutesPerInstance()-1);
        LocalDateTime end = start.plusMinutes(tournament.durationMinutesPerInstance());
        TournamentInstance tournamentInstance = new TournamentInstance(tournament.maxEntriesPerInstance(),start,close,end);
        tournamentInstance.owner(tournament.distributionKey());
        dataStore.create(tournamentInstance);
        return tournamentInstance;
    }

    @Override
    public Tournament.Entry create(String systemId, Tournament.Instance instance) {
        TournamentEntry entry = new TournamentEntry(systemId);
        entry.owner(instance.distributionKey());
        dataStore.create(entry);
        return entry;
    }
}
