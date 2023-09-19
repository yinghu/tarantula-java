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


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer buffer){
        return false;
    }
    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){

    }



    @Override
    public void onDeleting(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value) {

    }
    public void assignKey(Recoverable.DataBuffer dataBuffer){
        dataBuffer.writeLong(snowflakeIdGenerator.snowflakeId());
        //dataBuffer.writeUTF8(UUID.randomUUID().toString());
    }

    public long distributionId(){
        return snowflakeIdGenerator.snowflakeId();
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
