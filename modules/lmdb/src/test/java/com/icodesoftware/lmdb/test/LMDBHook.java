package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.*;
import com.icodesoftware.service.DataStoreProvider;

import org.testng.annotations.BeforeClass;


public class LMDBHook {

    protected DataStoreProvider lmdbDataStoreProvider;
    protected TestMapStoreListener testMapStoreListener;

    protected LocalDistributionIdGenerator localDistributionIdGenerator;

    @BeforeClass
    public void setUp() throws Exception{
        TestSetup.setUp();
        lmdbDataStoreProvider = TestSetup.lmdbDataStoreProvider;
        localDistributionIdGenerator = TestSetup.localDistributionIdGenerator;
        testMapStoreListener = TestSetup.testMapStoreListener;
    }

    int count(DataStore dataStore){
        int[] ct={0};
        dataStore.backup().forEach((k,v)->{
            ct[0]++;
            return true;
        });
        return ct[0];
    }

    int count(DataStore dataStore, Recoverable.Key key,String label){
        int[] ct={0};
        dataStore.backup().forEachEdgeKey(key,label,(k,v)->{
            ct[0]++;
            return true;
        });
        return ct[0];
    }
}
