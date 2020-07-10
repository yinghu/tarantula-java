package com.tarantula.platform.service.deployment;

import com.tarantula.RecoverableFactory;

import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.cluster.PortableRegistry;


/**
 * Updated by yinghu on 7/10/2020.
 */
public class LobbyQuery implements RecoverableFactory<LobbyDescriptor> {

    public static String LABEL = "LB";


    private   String bucketId;

    public LobbyQuery(String bucketId){
        this.bucketId  = bucketId;
    }

    public LobbyDescriptor create() {
        return  new LobbyDescriptor();
    }

    public String distributionKey() {
        return this.bucketId;
    }

    public  int registryId(){
        return PortableRegistry.LOBBY_CID;
    }


    public String label(){
        return LABEL;
    }


}
