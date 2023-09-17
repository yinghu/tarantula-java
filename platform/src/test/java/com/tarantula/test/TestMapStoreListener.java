package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.util.SnowflakeIdGenerator;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.util.SystemUtil;

import java.nio.ByteBuffer;
import java.util.UUID;

public class TestMapStoreListener implements MapStoreListener {

    SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
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



    public void onDistributing(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer value){}


    public boolean onRecovering(Metadata metadata, Recoverable.DataBuffer key, Recoverable.DataBuffer bufferStream){
        return false;
    }
    @Override
    public void onDeleting(Metadata metadata, Recoverable.DataBuffer key) {

    }
}
