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
    public int enter(String systemId) {
        return 0;
    }
    @Override
    public int enter(Session session) {
        if(tournamentManager.global()){
            System.out.println("skip enter proxy");
        }
        return 0;
    }
    @Override
    public boolean update(String systemId, Tournament.OnEntry onEntry) {
        return false;
    }

    @Override
    public boolean update(Session session, Tournament.OnEntry onEntry) {
        TournamentEntryProxy tournamentEntryProxy = new TournamentEntryProxy();
        onEntry.on(tournamentEntryProxy);
        if(tournamentEntryProxy.score()==tournamentManager.targetScore()){
            System.out.println("ON BOARD : "+tournamentManager.enter(session));
        }
        System.out.println("SCORE : "+tournamentEntryProxy.score()+" : "+tournamentManager.targetScore());
        return true;
    }

    @Override
    public Tournament.RaceBoard raceBoard() {
        return null;
    }
}
