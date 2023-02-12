package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

public class TournamentInstanceEndMonitor implements SchedulingTask {

    private final TournamentManager tournamentHeader;
    private final TournamentInstance tournamentInstanceHeader;

    public TournamentInstanceEndMonitor(TournamentManager tournamentHeader, TournamentInstance tournamentInstanceHeader){
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
        return TimeUtil.durationUTCMilliseconds(tournamentInstanceHeader.closeTime(),tournamentInstanceHeader.endTime());
    }

    @Override
    public void run() {
        tournamentHeader.tournamentInstanceEnded(tournamentInstanceHeader);
    }
}
