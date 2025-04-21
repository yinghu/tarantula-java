package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;
import com.icodesoftware.lmdb.*;
import com.icodesoftware.service.DataStoreProvider;

import org.testng.annotations.BeforeClass;

import java.util.List;


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
        testMapStoreListener.verifier = (scope,tid)->{
            List<Transaction.Log> logs = testMapStoreListener.transactionLogManager.committed(scope,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
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
