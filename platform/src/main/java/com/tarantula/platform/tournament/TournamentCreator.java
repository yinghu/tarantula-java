package com.tarantula.platform.tournament;

import com.icodesoftware.DataStore;
import com.icodesoftware.Tournament;
import com.tarantula.game.service.GameServiceProvider;

import java.time.LocalDateTime;
import java.util.List;

public class TournamentCreator implements Tournament.Creator {

    private final DataStore dataStore;
    private final Tournament.Listener listener;
    //private final GameServiceProvider gameServiceProvider;
    public TournamentCreator(DataStore dataStore, Tournament.Listener listener){
        this.dataStore = dataStore;
        this.listener = listener;

    }

    @Override
    public Tournament create(Tournament.Schedule schedule) {
        Tournament tournament = new DefaultTournament(schedule,this,listener);
        this.dataStore.create(tournament);
        return tournament;
    }

    @Override
    public Tournament load(String tournamentId) {
        DefaultTournament tournament = new DefaultTournament(this,listener);
        tournament.distributionKey(tournamentId);
        if(!dataStore.load(tournament)){
            return null;
        }
        TournamentInstanceQuery _q = new TournamentInstanceQuery(tournamentId);
        List<TournamentInstance> tlist = dataStore.list(_q);
        tlist.forEach((ti)->{
            dataStore.list(new TournamentEntryQuery(ti.id(),listener),(te)->{
                te.owner(ti.id());
                listener.onCreated(te);
                tournament.addTournamentEntry(te);
                ti.enter(te);
                return true;
            });
            ti.owner(tournamentId);
            this.listener.onStarted(ti);
            tournament.addTournamentInstance(ti);
        });
        return tournament;
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
