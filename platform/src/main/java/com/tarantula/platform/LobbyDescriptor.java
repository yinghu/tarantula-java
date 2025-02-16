package com.tarantula.platform;

import com.icodesoftware.protocol.service.TRDescriptor;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class LobbyDescriptor extends TRDescriptor {

    public static String LABEL = "lobby";
    public LobbyDescriptor(){
        super();
        this.label = LABEL;
    }
    @Override
    public int getClassId() {
        return PortableRegistry.LOBBY_CID;
    }
    @Override
    public int getFactoryId() {
        return PortableRegistry.OID;
    }


}
