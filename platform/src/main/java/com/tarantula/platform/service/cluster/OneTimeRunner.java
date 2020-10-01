package com.tarantula.platform.service.cluster;

import com.icodesoftware.SchedulingTask;

public class OneTimeRunner implements SchedulingTask {

    private final Runnable runnable;
    private final long delay;
    public OneTimeRunner(long delay,Runnable runnable){
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
