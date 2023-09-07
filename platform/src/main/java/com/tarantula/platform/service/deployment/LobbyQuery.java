package com.tarantula.platform.service.deployment;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.OidKey;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.cluster.PortableRegistry;


public class LobbyQuery implements RecoverableFactory<LobbyDescriptor> {



    private long ownerId;

    public LobbyQuery(long ownerId){
        this.ownerId  = ownerId;
    }

    public LobbyDescriptor create() {
        return  new LobbyDescriptor();
    }

    public String distributionKey() {
        return null;
    }

    public  int registryId(){
        return PortableRegistry.LOBBY_CID;
    }


    public String label(){
        return LobbyDescriptor.LABEL;
    }

    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(ownerId);
    }

}
