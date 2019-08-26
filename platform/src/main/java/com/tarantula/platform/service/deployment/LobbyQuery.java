package com.tarantula.platform.service.deployment;

import com.tarantula.RecoverableFactory;
import com.tarantula.platform.DeploymentDescriptor;
import com.tarantula.platform.service.cluster.PortableRegistry;


/**
 * Updated by yinghu on 4/8/2019.
 */
public class LobbyQuery implements RecoverableFactory<DeploymentDescriptor> {

    public  String bucket;

    public LobbyQuery(String bucket){
        this.bucket  = bucket;
    }

    public DeploymentDescriptor create() {
        DeploymentDescriptor lb =  new DeploymentDescriptor();
        lb.vertex("Lobby");
        return lb;
    }

    public String distributionKey() {
        return this.bucket;
    }

    public  int registryId(){
        return PortableRegistry.LOBBY_CID;
    }


    public String label(){
        return "LB";
    }


}
