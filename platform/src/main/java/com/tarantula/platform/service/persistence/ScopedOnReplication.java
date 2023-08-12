package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.OnReplication;

public interface ScopedOnReplication {

    ClusterProvider.Node node();
    void node(ClusterProvider.Node node);
    default void write(int partition,byte[] key,byte[] value){}
    default void write(String source,byte[] key,byte[] value){}
    OnReplication read();

    void drop();
}
