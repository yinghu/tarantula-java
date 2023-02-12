package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;

public class TournamentEndTask implements SchedulingTask {

    private TournamentManager tournamentHeader;
    public TournamentEndTask(TournamentManager tournamentHeader){
        this.tournamentHeader = tournamentHeader;
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
        tournamentHeader.end();
    }
}
