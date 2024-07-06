package com.icodesoftware.util;

import com.icodesoftware.service.ClusterProvider;

public class TarantulaAgent implements ClusterProvider.HomingAgent {

    public boolean enabled;
    public String host;
    public String accessKey;
    public String encryptionKey;
    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public String accessKey() {
        return accessKey;
    }

    @Override
    public String encryptionKey() {
        return encryptionKey;
    }
}
