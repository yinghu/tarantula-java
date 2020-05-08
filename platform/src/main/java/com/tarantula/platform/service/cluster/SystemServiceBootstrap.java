package com.tarantula.platform.service.cluster;

import com.tarantula.platform.service.Serviceable;
import com.tarantula.platform.TarantulaContext;

public class SystemServiceBootstrap implements Serviceable {

    private TarantulaContext tarantulaContext;
    public SystemServiceBootstrap(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }

    @Override
    public void start() throws Exception {
        this.tarantulaContext._setup();
        this.tarantulaContext.tokenValidatorProvider().setup(this.tarantulaContext);
        this.tarantulaContext.tokenValidatorProvider().waitForData();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
