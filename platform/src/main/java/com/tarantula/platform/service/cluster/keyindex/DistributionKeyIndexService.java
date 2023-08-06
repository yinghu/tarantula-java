package com.tarantula.platform.service.cluster.keyindex;

import com.icodesoftware.service.ServiceProvider;


public interface DistributionKeyIndexService extends ServiceProvider {

    String NAME = "DistributionKeyIndexService";

    byte[] recover(byte[] key);
}
