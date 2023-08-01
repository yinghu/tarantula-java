package com.tarantula.platform.service.cluster.keyindex;

import com.icodesoftware.service.Serviceable;

public class KeyIndexServiceBootstrap implements Serviceable {

    private final KeyIndexClusterService keyIndexService;
    public KeyIndexServiceBootstrap(final KeyIndexClusterService keyIndexService){
        this.keyIndexService = keyIndexService;

    }

    @Override
    public void start() throws Exception {
        keyIndexService.setup();
    }

    @Override
    public void shutdown() throws Exception {

    }
}
