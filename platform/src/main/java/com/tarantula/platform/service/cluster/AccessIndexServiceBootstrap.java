package com.tarantula.platform.service.cluster;

import com.tarantula.platform.service.Serviceable;

/**
 * Created by yinghu lu on 8/17/2018.
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
