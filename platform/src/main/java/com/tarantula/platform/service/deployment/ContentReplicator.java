package com.tarantula.platform.service.deployment;

import com.tarantula.SchedulingTask;

public class ContentReplicator implements SchedulingTask {
    private final String fileName;
    private final PlatformDeploymentServiceProvider platformDeploymentServiceProvider;
    public ContentReplicator(PlatformDeploymentServiceProvider deploymentServiceProvider,final String fname){
        this.fileName = fname;
        this.platformDeploymentServiceProvider = deploymentServiceProvider;
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
        this.platformDeploymentServiceProvider._pushContent(fileName);
    }
}
