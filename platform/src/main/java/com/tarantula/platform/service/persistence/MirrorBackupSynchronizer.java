package com.tarantula.platform.service.persistence;

import com.icodesoftware.SchedulingTask;

public class MirrorBackupSynchronizer implements SchedulingTask {

    private final MirrorClusterBackupProvider mirrorClusterBackupProvider;
    private final long nextSyncInterval;
    public MirrorBackupSynchronizer(MirrorClusterBackupProvider mirrorClusterBackupProvider,long interval){
        this.mirrorClusterBackupProvider = mirrorClusterBackupProvider;
        this.nextSyncInterval = interval;
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
        mirrorClusterBackupProvider._batch(this);
    }
}
