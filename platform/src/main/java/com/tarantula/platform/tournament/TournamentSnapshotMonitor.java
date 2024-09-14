package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;


public class TournamentSnapshotMonitor implements SchedulingTask {

    private final TournamentManager tournamentManager;
    private final PlatformTournamentServiceProvider tournamentServiceProvider;

    public TournamentSnapshotMonitor(TournamentManager tournamentManager, PlatformTournamentServiceProvider tournamentServiceProvider){
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
        try{
        this.tournamentServiceProvider.sortTournament(tournamentManager);
        }catch (Exception ex){
            this.tournamentServiceProvider.logger().error("error on snapshot tournament",ex);
        }
    }
}
