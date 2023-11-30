package com.tarantula.platform.tournament;

import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.time.LocalDateTime;

public class TournamentInstanceProxy extends RecoverableObject implements Tournament.Instance {

    private TournamentManager tournamentManager;

    public TournamentInstanceProxy(TournamentManager tournamentManager){
        this.tournamentManager = tournamentManager;
    }
    @Override
    public Tournament.Status status() {
        return Tournament.Status.STARTING;
    }

    @Override
    public int maxEntries() {
        return tournamentManager.maxEntriesPerInstance();
    }

    @Override
    public LocalDateTime startTime() {
        return tournamentManager.startTime();
    }

    @Override
    public LocalDateTime closeTime() {
        return tournamentManager.closeTime();
    }

    @Override
    public LocalDateTime endTime() {
        return tournamentManager.endTime();
    }



    @Override
    public boolean update(Session session, Tournament.OnEntry onEntry) {
        TournamentEntryProxy tournamentEntryProxy = new TournamentEntryProxy();
        onEntry.on(tournamentEntryProxy);
        if(tournamentEntryProxy.score() != tournamentManager.targetScore()) return false;
        return tournamentManager.enter(session);
    }

    @Override
    public Tournament.RaceBoard raceBoard() {
        return this.tournamentManager.raceBoard();
    }
}
