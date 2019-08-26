package com.tarantula.platform.service.deployment;

import com.tarantula.RecoverableFactory;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.service.cluster.PortableRegistry;


/**
 * Updated by yinghu on 4/8/2019.
 */
public class ApplicationQuery implements RecoverableFactory<DeploymentDescriptor> {

    private String lobbyId;

    public ApplicationQuery(String lobbyId){
        this.lobbyId = lobbyId;
    }


    public DeploymentDescriptor create() {
        DeploymentDescriptor app = new DeploymentDescriptor();
        return app;
    }


    public String distributionKey() {
        return lobbyId;
    }

    public  int registryId(){
        return PortableRegistry.APPLICATION_DESCRIPTOR_CID;
    }

    public String label(){
        return "LDA";
    }
}
