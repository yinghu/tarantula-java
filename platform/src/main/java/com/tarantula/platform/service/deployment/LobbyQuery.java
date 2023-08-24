package com.tarantula.platform.service.deployment;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.LongTypeKey;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.cluster.PortableRegistry;


public class LobbyQuery implements RecoverableFactory<LobbyDescriptor> {


    private   String bucketId;
    private long ownerId;
    public LobbyQuery(String bucketId){
        this.bucketId  = bucketId;
    }

    public LobbyQuery(long ownerId){
        this.ownerId  = ownerId;
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
        return LobbyDescriptor.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new LongTypeKey(ownerId);
    }

}
