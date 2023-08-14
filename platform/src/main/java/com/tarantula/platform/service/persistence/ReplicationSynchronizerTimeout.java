package com.tarantula.platform.service.persistence;


import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.ClusterProvider;

public class ReplicationSynchronizerTimeout implements SchedulingTask {

    private final ScopedReplicationProxy proxy;
    private final long nextSyncInterval;

    private final ClusterProvider.Node targetNode;
    public ReplicationSynchronizerTimeout(ScopedReplicationProxy proxy, long nextSyncInterval, ClusterProvider.Node target){
        this.proxy = proxy;
        this.nextSyncInterval = nextSyncInterval;
        this.targetNode = target;
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
        proxy.replicate(targetNode);
    }
}
