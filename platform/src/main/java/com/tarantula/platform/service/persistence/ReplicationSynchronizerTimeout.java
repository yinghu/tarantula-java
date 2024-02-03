package com.tarantula.platform.service.persistence;


import com.icodesoftware.SchedulingTask;

public class ReplicationSynchronizerTimeout implements SchedulingTask {

    private Runnable runnable;
    private long delay;

    public ReplicationSynchronizerTimeout(long delay,Runnable runnable){
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
        runnable.run();
    }
}
