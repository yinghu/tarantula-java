package com.tarantula.platform.tournament;

import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.time.LocalDateTime;

public class TournamentInstanceProxy extends RecoverableObject implements Tournament.Instance {

    private TournamentManager tournamentManager;
    private TournamentJoin instance;


    public TournamentInstanceProxy(TournamentManager tournamentManager,TournamentJoin instance){
        this.tournamentManager = tournamentManager;
        this.instance = instance;
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
        if(tournamentManager.global()){
            TournamentEntryProxy tournamentEntryProxy = new TournamentEntryProxy();
            onEntry.on(tournamentEntryProxy);
            if(tournamentManager.targetScore()>0){
                if(tournamentEntryProxy.score() != tournamentManager.targetScore()) return false;
                boolean finished = tournamentManager.enter(session);
                instance.finished();
                return finished;
            }
            boolean finished = tournamentManager.score(session,tournamentEntryProxy);
            if(finished) instance.finished();
            return finished;
        }
        TournamentEntryProxy tournamentEntryProxy = new TournamentEntryProxy();
        onEntry.on(tournamentEntryProxy);
        return tournamentManager.score(session,instance.instanceId,tournamentEntryProxy);
    }

    @Override
    public Tournament.RaceBoard raceBoard() {
        return this.tournamentManager.raceBoard(instance);
    }
}
