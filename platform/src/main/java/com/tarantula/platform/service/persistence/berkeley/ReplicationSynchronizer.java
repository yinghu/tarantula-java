package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.SchedulingTask;

public class ReplicationSynchronizer implements SchedulingTask {

    private final BerkeleyJEProvider berkeleyJEProvider;
    private final long nextSyncInterval;
    public ReplicationSynchronizer(BerkeleyJEProvider berkeleyJEProvider, long nextSyncInterval){
        this.berkeleyJEProvider = berkeleyJEProvider;
        this.nextSyncInterval = nextSyncInterval;
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
        return nextSyncInterval;
    }

    @Override
    public void run() {
        berkeleyJEProvider._replicate();
    }
}
