package com.tarantula.platform.service.cluster;

import com.tarantula.platform.service.Serviceable;

/**
 * Created by yinghu lu on 8/17/2018.
 */
public class DeployServiceBootstrap implements Serviceable {

    private final ClusterDeployService clusterDeployService;
    public DeployServiceBootstrap(final ClusterDeployService clusterDeployService){
        this.clusterDeployService = clusterDeployService;

    }

    @Override
    public void start() throws Exception {
        clusterDeployService.setup();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
