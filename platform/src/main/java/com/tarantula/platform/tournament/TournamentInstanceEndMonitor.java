package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;


public class TournamentInstanceEndMonitor implements SchedulingTask {

    private final TournamentManager tournamentHeader;
    private final long pendingInstanceId;
    private final long delay;

    public TournamentInstanceEndMonitor(TournamentManager tournamentHeader, long instanceId,long delay){
        this.tournamentHeader = tournamentHeader;
        this.pendingInstanceId = instanceId;
        this.delay = delay;
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
        return this.delay;
    }

    @Override
    public void run() {
        tournamentHeader.endTournamentInstance(pendingInstanceId);
    }
}
