package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;


public class QueryTest extends LMDBHook{

    @Test(groups = { "native data store" })
    public void queryTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_query_test_object");
        for(int i=0;i<10;i++){
            TestObject testObject = new TestObject("atype","aname");
            testObject.ownerKey(SnowflakeKey.from(100));
            testObject.onEdge(true);
            testObject.label("link");
            Assert.assertTrue(dataStore.create(testObject));
            Assert.assertTrue(dataStore.createEdge(testObject,"provider"));
            Assert.assertEquals(testObject.revision(), EnvSetting.REVISION_START);
            Assert.assertTrue(testObject.distributionId()>0);
        }
        Assert.assertEquals(count(dataStore),10);
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),"link"),10);
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),"provider"),10);

        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+dataStore.name());

        Assert.assertEquals(dataStore.list(new TestObjectQuery(100,"link")).size(),10);
        Assert.assertEquals(dataStore.list(new TestObjectQuery(100,"provider")).size(),10);
        Assert.assertEquals(index.list(new TestObjectQuery(100,"link")).size(),10);
        Assert.assertEquals(index.list(new TestObjectQuery(100,"provider")).size(),10);


    }

}
