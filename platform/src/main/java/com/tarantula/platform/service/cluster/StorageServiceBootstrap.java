package com.tarantula.platform.service.cluster;

import com.tarantula.platform.service.Serviceable;
import com.tarantula.platform.TarantulaContext;

public class StorageServiceBootstrap implements Serviceable {

    private TarantulaContext tarantulaContext;
    public StorageServiceBootstrap(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }

    @Override
    public void start() throws Exception {
        this.tarantulaContext._dataStoreProviderMap().forEach((k,v)->{ //synchronize data and setup
            v.setup(this.tarantulaContext);
            v.waitForData();//block for global data sync
        });
    }

    @Override
    public void shutdown() throws Exception {

    }
}
