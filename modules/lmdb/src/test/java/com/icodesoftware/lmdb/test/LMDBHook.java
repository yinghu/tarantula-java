package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.*;
import com.icodesoftware.service.DataStoreProvider;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;


public class LMDBHook {

    protected DataStoreProvider lmdbDataStoreProvider;
    protected TestMapStoreListener testMapStoreListener;

    protected LocalDistributionIdGenerator localDistributionIdGenerator;

    @BeforeClass
    public void setUp() throws Exception{
        TestSetup.setUp();
        lmdbDataStoreProvider = TestSetup.lmdbDataStoreProvider;
        localDistributionIdGenerator = TestSetup.localDistributionIdGenerator;//new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        testMapStoreListener = TestSetup.testMapStoreListener;//new TestMapStoreListener(lmdbDataStoreProvider);
    }
    @AfterTest
    public void tearDown() throws Exception{

        //lmdbDataStoreProvider.shutdown();
    }

    int count(DataStore dataStore){
        int[] ct={0};
        dataStore.backup().forEach((k,v)->{
            ct[0]++;
            return true;
        });
        return ct[0];
    }
}
