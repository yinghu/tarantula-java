package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

public class TournamentEndMonitor implements SchedulingTask {

    private final TournamentHeader tournamentHeader;
    private final PlatformTournamentServiceProvider distributedTournamentServiceProvider;

    public TournamentEndMonitor(TournamentHeader tournamentHeader, PlatformTournamentServiceProvider distributedTournamentServiceProvider){
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
        if(TimeUtil.expired(tournamentHeader.endTime)) return 3000;
        return TimeUtil.durationUTCMilliseconds(tournamentHeader.closeTime(),tournamentHeader.endTime());
    }

    @Override
    public void run() {
        this.distributedTournamentServiceProvider.onTournamentEnd(tournamentHeader);
    }
}
