package com.tarantula.platform.service;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class AccessKeyQuery implements RecoverableFactory<AccessKey> {

    public String owner;
    private Recoverable.Key key;
    public AccessKeyQuery(String owner){
        this.owner = owner;
    }

    public AccessKeyQuery(Recoverable.Key key){
        this.key = key;
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


    public String distributionKey() {
        return owner;
    }

    @Override
    public Recoverable.Key key() {
        return key;
    }
}
