package com.tarantula.platform;

import com.tarantula.platform.event.PortableEventRegistry;

public class ExposedGameService extends OnApplicationHeader {

    public static final String INDEX_LABEL = "IX";

    @Override
    public int getFactoryId() {
        return PortableEventRegistry.OID;
    }

    @Override
    public int getClassId() {
        return PortableEventRegistry.EXPOSED_GAME_SERVICE_CID;
    }


}
