package com.tarantula.platform.service;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;
import com.icodesoftware.util.SnowflakeKey;
import com.tarantula.platform.service.cluster.PortableRegistry;

public class AccessKeyQuery implements RecoverableFactory<AccessKey> {

    public long owner;
    private Recoverable.Key key;
    public AccessKeyQuery(long owner){
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
    public String label() {
        return AccessKey.LABEL;
    }
    @Override
    public Recoverable.Key key() {
        return new SnowflakeKey(owner);
    }
}
