package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;


public class TournamentStartMonitor implements SchedulingTask {

    private final TournamentManager tournamentManager;
    private final PlatformTournamentServiceProvider tournamentServiceProvider;

    public TournamentStartMonitor(TournamentManager tournamentManager, PlatformTournamentServiceProvider tournamentServiceProvider){
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
       return tournamentManager.toStartTime();
    }

    @Override
    public void run() {
        this.tournamentServiceProvider.startTournament(tournamentManager);
    }
}
