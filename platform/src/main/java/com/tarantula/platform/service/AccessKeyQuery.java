package com.tarantula.platform.service;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class AccessKeyQuery implements RecoverableFactory<AccessKey> {

    public String owner;

    public AccessKeyQuery(String owner){
        this.owner = owner;
    }

    @Override
    public AccessKey create() {
        return new AccessKey();
    }

    @Override
    public int registryId() {
        return PortableRegistry.ACCESS_KEY;
    }

    @Override
    public String label() {
        return AccessKey.LABEL;
    }

    @Override
    public String distributionKey() {
        return owner;
    }
}
