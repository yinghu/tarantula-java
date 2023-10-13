package com.tarantula.test.integration;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.SnowflakeIdGenerator;
import com.icodesoftware.util.TimeUtil;

import java.nio.ByteBuffer;

public class TestMapStoreListener implements MapStoreListener {

    LMDBDataStoreProvider provider;
    public SnowflakeIdGenerator snowflakeIdGenerator;
    public TestMapStoreListener(LMDBDataStoreProvider provider){
        this.provider = provider;
        snowflakeIdGenerator = new SnowflakeIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
    }

    public void onLogging(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){

    }
    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        return false;
    }
    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value,long transactionId){

    }

    @Override
    public void onCommit(long transactionId) {

    }

    @Override
    public void onAbort(long transactionId) {

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
