package com.icodesoftware.lmdb.partition;



public class PartitionTxn implements AutoCloseable {

    private final LMDBPartitionProvider lmdbPartitionProvider;
    public PartitionTxn(LMDBPartitionProvider lmdbPartitionProvider){
        this.lmdbPartitionProvider = lmdbPartitionProvider;
    }

    @Override
    public void close() throws Exception {

    }
    public void commit(){}
    public void abort(){}
    public long transactionId(){
        return 10;
    }
}
