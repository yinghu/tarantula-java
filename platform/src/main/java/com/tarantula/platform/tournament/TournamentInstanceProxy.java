package com.tarantula.platform.tournament;

import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.time.LocalDateTime;

public class TournamentInstanceProxy extends RecoverableObject implements Tournament.Instance {

    private TournamentManager tournamentManager;
    private TournamentInstance instance;

    private Session session;
    public TournamentInstanceProxy(TournamentManager tournamentManager,Session session){
        this.tournamentManager = tournamentManager;
        this.session = session;
    }
    public TournamentInstanceProxy(TournamentManager tournamentManager,Session session,TournamentInstance instance){
        this(tournamentManager,session);
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
                return tournamentManager.enter(session);
            }
            return tournamentManager.score(session,tournamentEntryProxy);
        }
        TournamentEntryProxy tournamentEntryProxy = new TournamentEntryProxy();
        onEntry.on(tournamentEntryProxy);
        return tournamentManager.score(session,instance.distributionId(),tournamentEntryProxy);
    }

    @Override
    public Tournament.RaceBoard raceBoard() {
        return this.tournamentManager.raceBoard(session);
    }
}
