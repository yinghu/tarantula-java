package com.tarantula.platform.tournament;

import com.icodesoftware.SchedulingTask;

public class TournamentEndTask implements SchedulingTask {
    @Override
    public boolean oneTime() {
        return false;
    }

    @Override
    public long initialDelay() {
        return 0;
    }

    @Override
    public long delay() {
        return 0;
    }

    @Override
    public void run() {

    }
}
