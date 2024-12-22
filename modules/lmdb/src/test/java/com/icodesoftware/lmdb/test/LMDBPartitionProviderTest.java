package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.lmdb.partition.LMDBPartitionDataStore;
import com.icodesoftware.lmdb.partition.LMDBPartitionProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LMDBPartitionProviderTest {


    @Test(groups = { "LMDBPartitionProviderTest" })
    public void initialTest() throws Exception{
        LocalDistributionIdGenerator localDistributionIdGenerator = new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        LMDBPartitionProvider lmdbPartitionProvider =  new LMDBPartitionProvider();
        lmdbPartitionProvider.registerDistributionIdGenerator(localDistributionIdGenerator);
        MapStoreListener testMapStoreListener = new TestMapStoreListener(lmdbPartitionProvider);
        lmdbPartitionProvider.registerMapStoreListener(Distributable.DATA_SCOPE,testMapStoreListener);
        lmdbPartitionProvider.start();
        DataStore dataStore = lmdbPartitionProvider.createDataStore("users");
        Assert.assertNotNull(dataStore);
        //TestObject testObject = new TestObject("type","name");
        //Assert.assertTrue(lmdbPartitionDataStore.create(testObject));
        //Assert.assertTrue(testObject.distributionId() >0);
        //TestObject loaded = new TestObject();
        //loaded.distributionId(testObject.distributionId());
        //Assert.assertTrue(lmdbPartitionDataStore.load(loaded));
        //Assert.assertTrue(lmdbPartitionDataStore.delete(loaded));
        //Assert.assertFalse(lmdbPartitionDataStore.load(loaded));
        lmdbPartitionProvider.shutdown();
    }
}
