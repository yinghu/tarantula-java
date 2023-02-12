package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.Tournament;


public class TournamentRegisterTask implements SchedulingTask {

    private final Tournament tournamentHeader;
    private final PlatformTournamentServiceProvider tournamentServiceProvider;

    public TournamentRegisterTask(TournamentManager tournamentHeader, PlatformTournamentServiceProvider tournamentServiceProvider){
        this.tournamentHeader = tournamentHeader;
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
        return 1000;
    }

    @Override
    public void run() {
        this.tournamentServiceProvider.onTournamentRegister(tournamentHeader);
    }
}
