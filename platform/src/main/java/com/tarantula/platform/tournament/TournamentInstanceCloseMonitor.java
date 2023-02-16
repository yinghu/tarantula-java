package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

public class TournamentInstanceCloseMonitor implements SchedulingTask {

    private final TournamentManager tournamentHeader;
    private final TournamentInstance tournamentInstanceHeader;

    public TournamentInstanceCloseMonitor(TournamentManager tournamentHeader, TournamentInstance tournamentInstanceHeader){
        this.tournamentHeader = tournamentHeader;
        this.tournamentInstanceHeader = tournamentInstanceHeader;
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
        return TimeUtil.durationUTCMilliseconds(tournamentInstanceHeader.startTime(),tournamentInstanceHeader.closeTime());
    }

    @Override
    public void run() {
        tournamentHeader.closeTournamentInstance(tournamentInstanceHeader);
    }
}
