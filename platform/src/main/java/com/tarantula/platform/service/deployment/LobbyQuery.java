package com.tarantula.platform.service.deployment;

import com.tarantula.RecoverableFactory;

import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.cluster.PortableRegistry;


/**
 * Updated by yinghu on 4/8/2019.
 */
public class LobbyQuery implements RecoverableFactory<LobbyDescriptor> {

    public  String bucket;

    public LobbyQuery(String bucket){
        this.bucket  = bucket;
    }

    public LobbyDescriptor create() {
        return  new LobbyDescriptor();
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
