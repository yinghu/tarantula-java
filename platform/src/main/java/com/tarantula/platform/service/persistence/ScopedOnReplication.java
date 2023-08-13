package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.OnReplication;

public interface ScopedOnReplication {

    ClusterProvider.Node node();

    default void write(ClusterProvider.Node node,int partition, byte[] key, byte[] value){}
    default void write(ClusterProvider.Node node,String source,byte[] key,byte[] value){}

    OnReplication read();

    void drop();
}
