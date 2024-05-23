package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;


public class TournamentSortingMonitor implements SchedulingTask {

    private final TournamentManager tournamentManager;
    private final PlatformTournamentServiceProvider tournamentServiceProvider;

    public TournamentSortingMonitor(TournamentManager tournamentManager, PlatformTournamentServiceProvider tournamentServiceProvider){
        this.tournamentManager = tournamentManager;
        this.tournamentServiceProvider = tournamentServiceProvider;
    }
    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
       return tournamentManager.toNextSortingTime();
    }

    @Override
    public void run() {
        this.tournamentServiceProvider.sortTournament(tournamentManager);
    }
}
