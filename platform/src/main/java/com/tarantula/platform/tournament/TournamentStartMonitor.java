package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class TournamentStartMonitor implements SchedulingTask {

    private final Tournament tournamentHeader;
    private final PlatformTournamentServiceProvider tournamentServiceProvider;

    public TournamentStartMonitor(Tournament tournamentHeader, PlatformTournamentServiceProvider tournamentServiceProvider){
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
        LocalDateTime current  = LocalDateTime.now();
        if(current.isAfter(tournamentHeader.startTime())) return 3000;
        return TimeUtil.durationUTCMilliseconds(current,tournamentHeader.startTime());
    }

    @Override
    public void run() {
        this.tournamentServiceProvider.onTournamentStart(tournamentHeader);
    }
}
