package com.icodesoftware.lmdb.test;

import com.icodesoftware.lmdb.*;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;


public class LMDBHook {

    protected LMDBDataStoreProvider lmdbDataStoreProvider;
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
}
