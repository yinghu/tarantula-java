package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;


public class TournamentCloseMonitor implements SchedulingTask {

    private final TournamentManager tournamentManager;
    private final PlatformTournamentServiceProvider tournamentServiceProvider;

    public TournamentCloseMonitor(TournamentManager tournamentManager, PlatformTournamentServiceProvider tournamentServiceProvider){
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
       return tournamentManager.toClosingTime();
    }

    @Override
    public void run() {
        try {
            this.tournamentServiceProvider.closeTournament(tournamentManager);
        }catch (Exception ex){
            this.tournamentServiceProvider.logger().error("error on close tournament",ex);
        }
    }
}
