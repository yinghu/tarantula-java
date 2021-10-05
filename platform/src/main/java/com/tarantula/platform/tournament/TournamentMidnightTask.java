package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;

public class TournamentMidnightTask implements SchedulingTask {

    private PlatformTournamentServiceProvider tournamentServiceProvider;
    public TournamentMidnightTask(PlatformTournamentServiceProvider tournamentServiceProvider){
        this.tournamentServiceProvider = tournamentServiceProvider;
    }
    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return 100;
    }

    @Override
    public long delay() {
        return 100;
    }

    @Override
    public void run() {
        tournamentServiceProvider.midnightCheck();
    }
}
