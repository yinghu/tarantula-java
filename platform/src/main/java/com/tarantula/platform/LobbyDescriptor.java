package com.tarantula.platform;

import com.tarantula.platform.service.cluster.PortableRegistry;

import java.util.Map;

public class LobbyDescriptor extends DefaultDescriptor {

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
