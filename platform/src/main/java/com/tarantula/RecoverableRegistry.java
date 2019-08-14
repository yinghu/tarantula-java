package com.tarantula;

import com.hazelcast.nio.serialization.PortableFactory;

/**
 * Created by yinghu lu on 3/31/2018.
 */
public interface RecoverableRegistry extends PortableFactory {
    int registryId();
}
