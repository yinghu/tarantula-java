package com.icodesoftware.lmdb.partition;

import com.icodesoftware.service.Serviceable;

import java.nio.ByteBuffer;

public interface LMDBPartition extends Serviceable {

    int partition();

    boolean put(String dbiName, ByteBuffer key, ByteBuffer value);

    ByteBuffer get(String dbiName, ByteBuffer key);

    boolean delete(String dbiName,ByteBuffer key);
    
}
