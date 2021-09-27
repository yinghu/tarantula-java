package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

import java.time.LocalDateTime;

public class TournamentRegistryCloseMonitor implements SchedulingTask {

    private final TournamentHeader tournamentHeader;
    private final TournamentRegistry tournamentRegistry;

    public TournamentRegistryCloseMonitor(TournamentHeader tournamentHeader, TournamentRegistry tournamentRegistry){
        this.tournamentHeader = tournamentHeader;
        this.tournamentRegistry = tournamentRegistry;
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
        return TimeUtil.durationUTCMilliseconds(LocalDateTime.now(),tournamentRegistry.closeTime());
    }

    @Override
    public void run() {
        tournamentHeader.tournamentRegistryClosed(tournamentRegistry);
    }
}