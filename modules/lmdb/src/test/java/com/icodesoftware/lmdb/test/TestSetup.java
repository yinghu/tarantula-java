package com.icodesoftware.lmdb.test;

import com.icodesoftware.Distributable;

import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;

import com.icodesoftware.lmdb.partition.LMDBPartitionProvider;
import com.icodesoftware.service.MapStoreListener;
import com.icodesoftware.util.TimeUtil;



public class TestSetup {
    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    static LMDBDataStoreProvider lmdbDataStoreProvider;
    static TestMapStoreListener testMapStoreListener;

    static LocalDistributionIdGenerator localDistributionIdGenerator;

    static boolean started = false;

    static LMDBPartitionProvider lmdbPartitionProvider;

    public static void setUp() throws Exception{
        if(started) return;
        started = true;
        lmdbDataStoreProvider = new LMDBDataStoreProvider();
        localDistributionIdGenerator = new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        lmdbDataStoreProvider.registerDistributionIdGenerator(localDistributionIdGenerator);
        lmdbDataStoreProvider.start();
        testMapStoreListener = new TestMapStoreListener(lmdbDataStoreProvider);
        lmdbDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,testMapStoreListener);
        lmdbDataStoreProvider.registerMapStoreListener(Distributable.INTEGRATION_SCOPE,testMapStoreListener);

        lmdbPartitionProvider =  new LMDBPartitionProvider();
        lmdbPartitionProvider.registerDistributionIdGenerator(localDistributionIdGenerator);
        MapStoreListener testMapStoreListener = new TestMapStoreListener(lmdbPartitionProvider);
        lmdbPartitionProvider.registerMapStoreListener(Distributable.DATA_SCOPE,testMapStoreListener);
        lmdbPartitionProvider.registerMapStoreListener(Distributable.INTEGRATION_SCOPE,testMapStoreListener);
        lmdbPartitionProvider.start();

    }

}
