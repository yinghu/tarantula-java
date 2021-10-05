package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class TournamentStartMonitor implements SchedulingTask {

    private final TournamentHeader tournamentHeader;
    private final PlatformTournamentServiceProvider distributedTournamentServiceProvider;

    public TournamentStartMonitor(TournamentHeader tournamentHeader, PlatformTournamentServiceProvider distributedTournamentServiceProvider){
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
        return TimeUtil.durationUTCMilliseconds(LocalDateTime.now(),tournamentHeader.startTime());
    }

    @Override
    public void run() {
        this.distributedTournamentServiceProvider.onTournamentStart(tournamentHeader);
    }
}
