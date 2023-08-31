package com.tarantula.platform.service.deployment;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.OidKey;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.service.ApplicationProvider;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class ApplicationQuery implements RecoverableFactory<DeploymentDescriptor> {

    //private String lobbyId;

    private String ownerId;


    public ApplicationQuery(String ownerId){
        this.ownerId = ownerId;
    }

    public DeploymentDescriptor create() {
        DeploymentDescriptor app = new DeploymentDescriptor();
        return app;
    }


    public String distributionKey() {
        return null;
    }

    public  int registryId(){
        return PortableRegistry.APPLICATION_DESCRIPTOR_CID;
    }

    public String label(){
        return ApplicationProvider.LABEL;
    }

    @Override
    public Recoverable.Key key(){
        return new OidKey(ownerId);
    }
}
