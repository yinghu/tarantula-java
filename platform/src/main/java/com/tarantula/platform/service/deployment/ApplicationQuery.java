package com.tarantula.platform.service.deployment;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.LongTypeKey;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.service.ApplicationProvider;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class ApplicationQuery implements RecoverableFactory<DeploymentDescriptor> {

    private String lobbyId;

    private long ownerId;

    public ApplicationQuery(String lobbyId){
        this.lobbyId = lobbyId;
    }

    public ApplicationQuery(long ownerId){
        this.ownerId = ownerId;
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
        return ApplicationProvider.LABEL;
    }

    @Override
    public Recoverable.Key key(){
        return new LongTypeKey(ownerId);
    }
}
