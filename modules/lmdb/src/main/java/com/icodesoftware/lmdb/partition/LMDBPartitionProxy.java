package com.icodesoftware.lmdb.partition;

import java.nio.ByteBuffer;


public class LMDBPartitionProxy implements LMDBPartition {

    private final int partition;

    public LMDBPartitionProxy(int partition){
        this.partition = partition;
    }

    public int partition(){
        return partition;
    }

    @Override
    public boolean put(String dbiName, ByteBuffer key, ByteBuffer value) {

        return true;
    }

    @Override
    public ByteBuffer get(String dbiName, ByteBuffer key) {

        return null;
    }

    @Override
    public boolean delete(String dbiName, ByteBuffer key) {
        return true;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
