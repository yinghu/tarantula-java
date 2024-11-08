package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.lmdb.partition.LMDBPartitionDaemon;
import com.icodesoftware.lmdb.partition.LMDBPartitionProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LMDBPartitionProviderTest {


    @Test(groups = { "LMDBPartitionProviderTest" })
    public void initialTest(){
        LMDBPartitionProvider lmdbPartitionProvider = new LMDBPartitionProvider();
        Exception exception = null;
        try{
            lmdbPartitionProvider.start();
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
        Recoverable.DataBuffer key = BufferProxy.buffer(100,true);
        key.writeUTF8("key1000");
        LMDBPartitionDaemon lmdbPartition = lmdbPartitionProvider.partition(key.flip());
        lmdbPartitionProvider.onPut(lmdbPartition,key.rewind());
        LMDBPartitionDaemon loaded = lmdbPartitionProvider.partition(key.rewind());
        Assert.assertEquals(loaded.partition,lmdbPartition.partition);
    }
}
