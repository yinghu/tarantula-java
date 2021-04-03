package com.tarantula.platform.service.cluster.deployment;

import com.icodesoftware.service.Serviceable;


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
