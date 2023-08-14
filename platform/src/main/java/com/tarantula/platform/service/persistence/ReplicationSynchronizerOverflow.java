package com.tarantula.platform.service.persistence;


import com.icodesoftware.Event;
import com.icodesoftware.SchedulingTask;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.ServiceContext;

public class ReplicationSynchronizerOverflow implements SchedulingTask {

    private final ServiceContext proxy;
    private final long nextSyncInterval;

    private final Event event;
    public ReplicationSynchronizerOverflow(ServiceContext proxy, long nextSyncInterval,Event event){
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
        proxy.clusterProvider().publisher().publish(event);
    }
}
