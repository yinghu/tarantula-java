package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Distributable;
import com.icodesoftware.SchedulingTask;

public class ReplicationSynchronizer implements SchedulingTask {

    private final BerkeleyJEProvider berkeleyJEProvider;
    private final long nextSyncInterval;
    private final int scope;
    public ReplicationSynchronizer(BerkeleyJEProvider berkeleyJEProvider, long nextSyncInterval,int scope){
        this.berkeleyJEProvider = berkeleyJEProvider;
        this.nextSyncInterval = nextSyncInterval;
        this.scope = scope;
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
        if(this.scope == Distributable.DATA_SCOPE){
            //berkeleyJEProvider._replicateOnDataScope(this);
        }
        else if(this.scope == Distributable.INTEGRATION_SCOPE){
            //berkeleyJEProvider._replicateOnIntegrationScope(this);
        }
    }
}
