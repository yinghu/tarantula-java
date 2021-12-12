package com.tarantula.platform.service.cluster;

import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.TarantulaContext;

public class StorageServiceBootstrap implements Serviceable {

    private TarantulaContext tarantulaContext;
    public StorageServiceBootstrap(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }

    @Override
    public void start() throws Exception {
       this.tarantulaContext.deploymentDataStoreProvider.waitForData();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
