package com.tarantula.platform.presence;

import com.icodesoftware.service.ServiceProvider;

public interface DistributionPresenceService extends ServiceProvider {

    String NAME = "DistributionPresenceService";

    int profileSequence(String serviceName,String name);
}
