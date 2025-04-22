package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;


public class DeleteEdgeTest extends LMDBHook{

    @Test(groups = { "native data store" })
    public void deleteEdgeTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_delete_edge_test_object");
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

        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+dataStore.name());
        Assert.assertEquals(count(index),10);
        Assert.assertEquals(count(index,SnowflakeKey.from(100),"link"),10);
        Assert.assertEquals(count(index,SnowflakeKey.from(100),"provider"),10);


        Assert.assertTrue(dataStore.deleteEdge(SnowflakeKey.from(100),"link"));
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),"link"),0);
        Assert.assertEquals(count(index,SnowflakeKey.from(100),"link"),0);
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),"provider"),10);
        Assert.assertEquals(count(index,SnowflakeKey.from(100),"provider"),10);

        Assert.assertTrue(dataStore.deleteEdge(SnowflakeKey.from(100),"provider"));
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),"provider"),0);
        Assert.assertEquals(count(index,SnowflakeKey.from(100),"provider"),0);

        Assert.assertEquals(count(dataStore),10);
        Assert.assertEquals(count(index),10);

    }

}
