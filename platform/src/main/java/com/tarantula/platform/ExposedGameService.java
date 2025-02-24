package com.tarantula.platform;

import com.icodesoftware.util.TROnApplication;
import com.tarantula.platform.event.PortableEventRegistry;

public class ExposedGameService extends TROnApplication {

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
