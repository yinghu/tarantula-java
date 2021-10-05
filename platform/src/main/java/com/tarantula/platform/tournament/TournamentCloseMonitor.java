package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

public class TournamentCloseMonitor implements SchedulingTask {

    private final TournamentHeader tournamentHeader;
    private final PlatformTournamentServiceProvider distributedTournamentServiceProvider;

    public TournamentCloseMonitor(TournamentHeader tournamentHeader, PlatformTournamentServiceProvider distributedTournamentServiceProvider){
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
        return TimeUtil.durationUTCMilliseconds(tournamentHeader.startTime(),tournamentHeader.closeTime());
    }

    @Override
    public void run() {
        this.distributedTournamentServiceProvider.onTournamentClose(tournamentHeader);
    }
}
