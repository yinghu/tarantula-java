package com.tarantula.platform.service.cluster.accessindex;

import com.icodesoftware.service.Serviceable;

/**
 * Updated by yinghu lu on 7/10/2020
 */
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
