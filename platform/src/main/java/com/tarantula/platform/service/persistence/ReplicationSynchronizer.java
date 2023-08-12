package com.tarantula.platform.service.persistence;


import com.icodesoftware.SchedulingTask;

public class ReplicationSynchronizer implements SchedulingTask {

    private final ScopedReplicationProxy scopedReplicationProxy;
    private final long nextSyncInterval;

    public ReplicationSynchronizer(ScopedReplicationProxy scopedReplicationProxy,long nextSyncInterval){
        this.scopedReplicationProxy = scopedReplicationProxy;
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
        scopedReplicationProxy.sync();
    }
}
