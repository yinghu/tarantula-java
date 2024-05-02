package com.tarantula.platform.tournament;

import com.icodesoftware.Session;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.RecoverableObject;

import java.time.LocalDateTime;

public class TournamentInstanceProxy extends RecoverableObject implements Tournament.Instance {

    private TournamentManager tournamentManager;
    private TournamentJoin tournamentJoin;


    public TournamentInstanceProxy(TournamentManager tournamentManager,TournamentJoin tournamentJoin){
        this.tournamentManager = tournamentManager;
        this.tournamentJoin = tournamentJoin;
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
                if(tournamentJoin.finished || tournamentEntryProxy.score() != tournamentManager.targetScore()) return false;
                tournamentManager.enter(session);
                tournamentJoin.finished();
                return true;
            }
            boolean finished = tournamentManager.score(session,tournamentEntryProxy);
            if(finished) tournamentJoin.finished();
            return finished;
        }
        TournamentEntryProxy tournamentEntryProxy = new TournamentEntryProxy();
        onEntry.on(tournamentEntryProxy);
        return tournamentManager.score(session,tournamentJoin.instanceId,tournamentEntryProxy);
    }

    @Override
    public Tournament.RaceBoard raceBoard() {
        return this.tournamentManager.raceBoard(tournamentJoin);
    }

    public Tournament.RaceBoard myRaceBoard(){
        return this.tournamentManager.myRaceBoard(tournamentJoin);
    }
}
