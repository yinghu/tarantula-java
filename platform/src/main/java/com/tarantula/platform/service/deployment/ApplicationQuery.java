package com.tarantula.platform.service.deployment;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.service.Application;
import com.tarantula.platform.service.cluster.PortableRegistry;

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
        return Application.LABEL;
    }
}
