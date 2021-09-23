package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;

public class TournamentRegistryCloseMonitor implements SchedulingTask {

    private final TournamentHeader tournamentHeader;
    private final TournamentRegistry tournamentInstanceHeader;

    public TournamentRegistryCloseMonitor(TournamentHeader tournamentHeader, TournamentRegistry tournamentInstanceHeader){
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
        return (tournamentHeader.durationMinutesPerInstance()-3)*1000*60;
    }

    @Override
    public void run() {
        tournamentHeader.tournamentRegistryClosed(tournamentInstanceHeader);
    }
}