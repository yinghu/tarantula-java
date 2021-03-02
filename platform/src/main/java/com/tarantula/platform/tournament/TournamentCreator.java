package com.tarantula.platform.tournament;

import com.icodesoftware.DataStore;
import com.icodesoftware.Tournament;

import java.time.LocalDateTime;

public class TournamentCreator implements Tournament.Creator {

    private final DataStore dataStore;
    private final Tournament.Listener listener;
    public TournamentCreator(DataStore dataStore, Tournament.Listener listener){
        this.dataStore = dataStore;
        this.listener = listener;
    }

    @Override
    public Tournament create(String type, Tournament.Schedule schedule) {
        Tournament tournament = new DefaultTournament(type,schedule,this,listener);
        this.dataStore.create(tournament);
        return tournament;
    }

    @Override
    public Tournament load(String tournamentId) {
        Tournament tournament = new DefaultTournament(this,listener);
        tournament.distributionKey(tournamentId);
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
        TournamentEntry entry = new TournamentEntry(systemId,listener);
        entry.owner(instance.distributionKey());
        dataStore.create(entry);
        return entry;
    }
}
