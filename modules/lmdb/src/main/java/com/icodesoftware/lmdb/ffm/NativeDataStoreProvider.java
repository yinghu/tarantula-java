package com.icodesoftware.lmdb.ffm;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.util.TimeUtil;

import java.util.concurrent.ArrayBlockingQueue;

public class NativeDataStoreProvider {

    private int KEY_SIZE = 200;
    private int VALUE_SIZE = 1780;

    private ArrayBlockingQueue<MemorySegmentCache> pendingQueue = new ArrayBlockingQueue<>(10);
    private DataStoreProvider.DistributionIdGenerator distributionIdGenerator = new LocalDistributionIdGenerator(1,TimeUtil.epochMillisecondsFromMidnight(2020,1,1));;

    public void registerDistributionIdGenerator(DataStoreProvider.DistributionIdGenerator distributionIdGenerator){
        if(distributionIdGenerator!=null) this.distributionIdGenerator = distributionIdGenerator;
    }

    public void assign(Recoverable.DataBuffer dataBuffer){
        this.distributionIdGenerator.assign(dataBuffer);
    }
    public Recoverable.DataBufferPair dataBufferPair(){
        MemorySegmentCache cache = pendingQueue.poll();
        if(cache!=null) return cache;
        return new MemorySegmentCache(KEY_SIZE,VALUE_SIZE,pendingQueue);
    }

}
