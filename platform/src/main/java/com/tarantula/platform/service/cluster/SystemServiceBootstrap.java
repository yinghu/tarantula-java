package com.tarantula.platform.service.cluster;

import com.icodesoftware.service.Serviceable;
import com.tarantula.platform.TarantulaContext;

public class SystemServiceBootstrap implements Serviceable {

    private TarantulaContext tarantulaContext;
    public SystemServiceBootstrap(TarantulaContext tarantulaContext){
        this.tarantulaContext = tarantulaContext;
    }

    @Override
    public void start() throws Exception {
        this.tarantulaContext._setup();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
