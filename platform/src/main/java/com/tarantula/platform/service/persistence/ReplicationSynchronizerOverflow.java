package com.tarantula.platform.service.persistence;

import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.event.EventOnReplication;

public class ReplicationSynchronizerOverflow implements SchedulingTask {

    private final ServiceContext proxy;
    private final long nextSyncInterval;

    private final EventOnReplication event;
    public ReplicationSynchronizerOverflow(ServiceContext proxy, long nextSyncInterval,EventOnReplication event){
        this.proxy = proxy;
        this.nextSyncInterval = nextSyncInterval;
        this.event = event;
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
        event.drain();
        proxy.clusterProvider().publisher().publish(event);
    }
}
