package com.icodesoftware.lmdb;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.util.SnowflakeIdGenerator;

public class LocalDistributionIdGenerator implements DataStoreProvider.DistributionIdGenerator {

    private SnowflakeIdGenerator snowflakeIdGenerator;
    public LocalDistributionIdGenerator(int nodeNumber,long epochStart){
        snowflakeIdGenerator = new SnowflakeIdGenerator(nodeNumber,epochStart);
    }
    @Override
    public long id() {
        return snowflakeIdGenerator.snowflakeId();
    }

    @Override
    public void assign(Recoverable.DataBuffer dataBuffer) {
        dataBuffer.writeLong(snowflakeIdGenerator.snowflakeId());
    }
}
