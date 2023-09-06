package com.icodesoftware.lmdb.test;

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
    @Override
    public <T extends Recoverable> void onBackingUp(Metadata metadata, String key, T t) {

    }

    @Override
    public void onDistributing(Metadata metadata, String stringKey, byte[] key, byte[] value) {

    }

    @Override
    public void onDistributing(Metadata metadata, ByteBuffer key, ByteBuffer value) {
        DataStore ds = provider.createDataStore("user_backup");
        ds.backup().set(key,value);
    }

    @Override
    public byte[] onRecovering(Metadata metadata, String stringKey, byte[] key) {
        return new byte[0];
    }

    @Override
    public void onDeleting(Metadata metadata, byte[] key) {

    }
    public void assignKey(Recoverable.DataBuffer dataBuffer){
        dataBuffer.writeLong(snowflakeIdGenerator.snowflakeId());
        //dataBuffer.writeUTF8(UUID.randomUUID().toString());
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
