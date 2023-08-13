package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.OnReplication;

public interface ScopedOnReplication {

    default void write(String sourceNode,int partition, byte[] key, byte[] value){}
    default void write(String sourceNode,String source,byte[] key,byte[] value){}

    OnReplication read();

    void drop();
}
