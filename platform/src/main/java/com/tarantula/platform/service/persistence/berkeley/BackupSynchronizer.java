package com.tarantula.platform.service.persistence.berkeley;

import com.icodesoftware.Distributable;
import com.icodesoftware.SchedulingTask;

public class BackupSynchronizer implements SchedulingTask {

    private final BerkeleyJEProvider berkeleyJEProvider;
    private final long nextSyncInterval;
    private final int scope;
    public BackupSynchronizer(BerkeleyJEProvider berkeleyJEProvider, long nextSyncInterval,int scope){
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
        if(scope == Distributable.DATA_SCOPE){
            berkeleyJEProvider._backupOnDataScope();
        }
        else if(scope==Distributable.INTEGRATION_SCOPE){
            berkeleyJEProvider._backupOnIntegrationScope();
        }
    }
}
