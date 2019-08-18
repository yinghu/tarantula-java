package com.tarantula.platform.service.cluster;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;
import com.tarantula.RecoverableRegistry;

public class DynamicPortableRegistry implements PortableFactory {

    private final RecoverableRegistry recoverableRegistry;
    public DynamicPortableRegistry(final RecoverableRegistry recoverableRegistry){
        this.recoverableRegistry = recoverableRegistry;
    }

    @Override
    public Portable create(int i) {
        return this.recoverableRegistry.create(i);
    }
}
