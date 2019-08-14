package com.tarantula.platform.service.cluster;

import com.tarantula.Serviceable;
import com.tarantula.platform.TarantulaContext;

public class SystemServiceBootstrap implements Serviceable {

    private TarantulaContext tarantulaContext;
    public SystemServiceBootstrap(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }

    @Override
    public void start() throws Exception {
        this.tarantulaContext.serviceProviders.forEach((k,v)->{ //synchronize data and setup
            v.setup(this.tarantulaContext);
            v.waitForData();//block for global data sync
        });
        this.tarantulaContext.tokenValidatorProvider.setup(this.tarantulaContext);
        this.tarantulaContext.tokenValidatorProvider.waitForData();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
