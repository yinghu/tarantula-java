package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.*;
import com.icodesoftware.util.BinaryKey;
import com.tarantula.platform.service.KeyIndexTrack;

public class IntegrationScopeReplicationProxy extends ScopedReplicationProxy {

    private TarantulaLogger logger = JDKLogger.getLogger(IntegrationScopeReplicationProxy.class);

    public IntegrationScopeReplicationProxy(){
        super(Distributable.INTEGRATION_SCOPE);
    }


}
