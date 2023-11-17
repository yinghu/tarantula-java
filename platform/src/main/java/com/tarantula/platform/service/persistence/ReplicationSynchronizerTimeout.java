package com.tarantula.platform.service.persistence;


import com.icodesoftware.SchedulingTask;

public class ReplicationSynchronizerTimeout implements SchedulingTask {

    private Runnable runnable;

    public ReplicationSynchronizerTimeout(Runnable runnable){
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
        return 100;
    }

    @Override
    public void run() {
        runnable.run();
    }
}
