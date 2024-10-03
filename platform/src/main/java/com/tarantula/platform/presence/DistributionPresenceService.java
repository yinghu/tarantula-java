package com.tarantula.platform.presence;

import com.icodesoftware.service.ServiceProvider;

import java.util.List;

public interface DistributionPresenceService extends ServiceProvider {

    String NAME = "DistributionPresenceService";

    int profileSequence(String serviceName,String name);

    public List<Boolean> deleteUserLoginData(long systemId);
}
