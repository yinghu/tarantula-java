package com.tarantula.test.integration;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.SnowflakeIdGenerator;
import com.icodesoftware.util.TimeUtil;


public class TestMapStoreListener implements MapStoreListener {

    LMDBDataStoreProvider provider;
    public SnowflakeIdGenerator snowflakeIdGenerator;
    public TestMapStoreListener(LMDBDataStoreProvider provider){
        this.provider = provider;
        snowflakeIdGenerator = new SnowflakeIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
    }

    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer,DataStore.BufferStream bufferStream){
        return false;
    }
    public void onUpdating(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){

    }

    @Override
    public void onCommit(int scope,long transactionId) {

    }

    @Override
    public void onAbort(int scope,long transactionId) {

    }

    @Override
    public boolean onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId) {
        return false;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
