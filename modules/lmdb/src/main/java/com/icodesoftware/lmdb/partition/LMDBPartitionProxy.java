package com.icodesoftware.lmdb.partition;

import java.nio.ByteBuffer;

public class LMDBPartitionProxy implements LMDBPartition {

    private int partition;

    public int partition(){
        return partition;
    }

    @Override
    public boolean put(String dbiName, ByteBuffer key, ByteBuffer value) {
        return false;
    }

    @Override
    public ByteBuffer get(String dbiName, ByteBuffer key) {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
