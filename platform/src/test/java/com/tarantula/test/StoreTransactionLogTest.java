package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.OnAccess;
import com.tarantula.platform.store.StoreTransactionLog;
import com.tarantula.platform.store.StoreTransactionQuery;
import org.testng.Assert;
import org.testng.annotations.Test;


public class StoreTransactionLogTest extends DataStoreHook{


    @Test(groups = { "PresenceKey" })
    public void storeTransactionLogTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_store_transaction_log");
        long itemId = serviceContext.distributionId();
        long systemId = serviceContext.distributionId();

        Assert.assertTrue(dataStore.createIfAbsent(new StoreTransactionLog(systemId,OnAccess.APPLE_STORE,"transactionId1",itemId,true),false));

        Assert.assertTrue(dataStore.load(new StoreTransactionLog("transactionId1")));

        Assert.assertEquals(dataStore.list(new StoreTransactionQuery(systemId)).size(),1);

        Assert.assertTrue(dataStore.createIfAbsent(new StoreTransactionLog(systemId,OnAccess.APPLE_STORE,"transactionId2",itemId,true),false));

        Assert.assertTrue(dataStore.load(new StoreTransactionLog("transactionId2")));

        Assert.assertEquals(dataStore.list(new StoreTransactionQuery(systemId)).size(),2);

        StoreTransactionLog loaded = new StoreTransactionLog("transactionId1");
        dataStore.load(loaded);
        Assert.assertEquals(loaded.playerId,systemId);
        Assert.assertEquals(loaded.itemId,itemId);
        Assert.assertEquals(loaded.storeName,OnAccess.APPLE_STORE);
        Assert.assertTrue(loaded.timestamp()>0);
        Assert.assertTrue(loaded.granted);

    }

}
