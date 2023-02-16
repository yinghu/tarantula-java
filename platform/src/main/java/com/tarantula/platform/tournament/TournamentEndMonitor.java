package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

public class TournamentEndMonitor implements SchedulingTask {

    private final TournamentManager tournamentHeader;
    private final PlatformTournamentServiceProvider distributedTournamentServiceProvider;

    public TournamentEndMonitor(TournamentManager tournamentHeader, PlatformTournamentServiceProvider distributedTournamentServiceProvider){
        this.tournamentHeader = tournamentHeader;
        this.distributedTournamentServiceProvider = distributedTournamentServiceProvider;
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
        return tournamentHeader.toEndingTime();
    }

    @Override
    public void run() {
        this.distributedTournamentServiceProvider.onTournamentEnd(tournamentHeader);
    }
}
