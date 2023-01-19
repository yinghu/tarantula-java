package com.tarantula.platform;

import com.icodesoftware.SchedulingTask;

public class ScheduleRunner implements SchedulingTask {

    private final Runnable runnable;
    private final long delay;

    public ScheduleRunner(long delay, Runnable runnable){
        this.delay = delay;
        this.runnable = runnable;
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
        return delay;
    }

    @Override
    public void run() {
        this.runnable.run();
    }
}
