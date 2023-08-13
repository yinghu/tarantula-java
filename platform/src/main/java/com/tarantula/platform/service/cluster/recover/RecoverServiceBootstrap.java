package com.tarantula.platform.service.cluster.recover;

import com.icodesoftware.service.Serviceable;

public class RecoverServiceBootstrap implements Serviceable {

    private final ClusterRecoverService clusterRecoverService;
    public RecoverServiceBootstrap(final ClusterRecoverService clusterRecoverService){
        this.clusterRecoverService = clusterRecoverService;

    }

    @Override
    public void start() throws Exception {
        clusterRecoverService.setup();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
