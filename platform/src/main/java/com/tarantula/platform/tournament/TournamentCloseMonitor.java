package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

public class TournamentCloseMonitor implements SchedulingTask {

    private final TournamentHeader tournamentHeader;
    private final PlatformTournamentServiceProvider tournamentServiceProvider;

    public TournamentCloseMonitor(TournamentHeader tournamentHeader, PlatformTournamentServiceProvider tournamentServiceProvider){
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
        if(TimeUtil.expired(tournamentHeader.closeTime)) return 3000;
        return TimeUtil.durationUTCMilliseconds(tournamentHeader.startTime(),tournamentHeader.closeTime());
    }

    @Override
    public void run() {
        this.tournamentServiceProvider.onTournamentClose(tournamentHeader);
    }
}
