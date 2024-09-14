package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.util.TimeUtil;

public class TournamentEndMonitor implements SchedulingTask {

    private final TournamentManager tournamentHeader;
    private final PlatformTournamentServiceProvider tournamentServiceProvider;

    public TournamentEndMonitor(TournamentManager tournamentHeader, PlatformTournamentServiceProvider tournamentServiceProvider){
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
        return tournamentHeader.toEndingTime();
    }

    @Override
    public void run() {
        try{
            this.tournamentServiceProvider.endTournament(tournamentHeader);
        }
        catch (Exception ex){
            this.tournamentServiceProvider.logger().error("error on end tournament",ex);
        }
    }
}
