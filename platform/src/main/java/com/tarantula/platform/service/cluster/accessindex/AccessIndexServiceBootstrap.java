package com.tarantula.platform.service.cluster.accessindex;

import com.icodesoftware.service.Serviceable;

public class AccessIndexServiceBootstrap implements Serviceable {

    private final AccessIndexClusterService accessIndexService;
    public AccessIndexServiceBootstrap(final AccessIndexClusterService accessIndexService){
        this.accessIndexService = accessIndexService;

    }

    @Override
    public void start() throws Exception {
        accessIndexService.setup();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
