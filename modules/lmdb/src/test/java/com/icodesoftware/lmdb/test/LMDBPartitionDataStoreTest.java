package com.icodesoftware.lmdb.test;

import com.icodesoftware.lmdb.partition.LMDBPartitionDataStore;
import com.icodesoftware.lmdb.partition.LMDBPartitionProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LMDBPartitionDataStoreTest {


    @Test(groups = { "LMDBPartitionProviderTest" })
    public void initialTest() throws Exception{
        LMDBPartitionProvider lmdbPartitionProvider =  LMDBPartitionProvider.create(false);
        lmdbPartitionProvider.start();
        LMDBPartitionDataStore lmdbPartitionDataStore = new LMDBPartitionDataStore(lmdbPartitionProvider);
        TestObject testObject = new TestObject("type","name");
        Assert.assertTrue(lmdbPartitionDataStore.create(testObject));
        Assert.assertTrue(testObject.distributionId() >0);
        TestObject loaded = new TestObject();
        loaded.distributionId(testObject.distributionId());
        Assert.assertTrue(lmdbPartitionDataStore.load(loaded));
        Assert.assertTrue(lmdbPartitionDataStore.delete(loaded));
        Assert.assertFalse(lmdbPartitionDataStore.load(loaded));
        lmdbPartitionProvider.shutdown();
    }
}
