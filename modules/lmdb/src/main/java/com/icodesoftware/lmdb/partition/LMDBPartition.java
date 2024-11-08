package com.icodesoftware.lmdb.partition;

import java.nio.ByteBuffer;

public interface LMDBPartition {

    boolean put(String dbiName, ByteBuffer key, ByteBuffer value);

    ByteBuffer get(String dbiName, ByteBuffer key);
}
