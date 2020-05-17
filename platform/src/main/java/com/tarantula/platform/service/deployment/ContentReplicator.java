package com.tarantula.platform.service.deployment;

import com.tarantula.SchedulingTask;

public class ContentReplicator implements SchedulingTask {
    private final String fileName;
    public ContentReplicator(final String fname){
        this.fileName = fname;
    }

    @Override
    public boolean oneTime() {
        return true;
    }

    @Override
    public long initialDelay() {
        return 1000;
    }

    @Override
    public long delay() {
        return 1000;
    }

    @Override
    public void run() {
        System.out.println(fileName);
    }
}
