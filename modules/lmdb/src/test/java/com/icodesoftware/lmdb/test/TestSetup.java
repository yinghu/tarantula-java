package com.icodesoftware.lmdb.test;

import com.icodesoftware.Distributable;

import com.icodesoftware.lmdb.LocalDistributionIdGenerator;

import com.icodesoftware.lmdb.ffm.NativeDataStoreProvider;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.util.TimeUtil;



public class TestSetup {
    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    static DataStoreProvider lmdbDataStoreProvider;
    static TestMapStoreListener testMapStoreListener;

    static LocalDistributionIdGenerator localDistributionIdGenerator;

    static boolean started = false;



    public static void setUp() throws Exception{
        if(started) return;
        started = true;
        lmdbDataStoreProvider = new NativeDataStoreProvider();
        localDistributionIdGenerator = new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        lmdbDataStoreProvider.registerDistributionIdGenerator(localDistributionIdGenerator);
        lmdbDataStoreProvider.start();
        testMapStoreListener = new TestMapStoreListener(lmdbDataStoreProvider);
        lmdbDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,testMapStoreListener);
        lmdbDataStoreProvider.registerMapStoreListener(Distributable.INTEGRATION_SCOPE,testMapStoreListener);
    }

}
