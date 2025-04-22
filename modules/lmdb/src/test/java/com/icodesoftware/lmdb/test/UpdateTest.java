package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;


public class UpdateTest extends LMDBHook{

    @Test(groups = { "native data store" })
    public void updateTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_update_test_object");
        TestObject testObject = new TestObject("atype","aname");
        testObject.ownerKey(SnowflakeKey.from(100));
        testObject.onEdge(true);
        testObject.label("link");
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertEquals(testObject.revision(), EnvSetting.REVISION_START);
        Assert.assertTrue(testObject.distributionId()>0);
        Assert.assertEquals(count(dataStore),1);
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),"link"),1);
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+dataStore.name());
        TestObject rep = new TestObject();
        rep.distributionId(testObject.distributionId());
        Assert.assertTrue(index.load(rep));
        Assert.assertEquals(count(index),1);
        Assert.assertEquals(count(index,SnowflakeKey.from(100),"link"),1);
        TestObject update = new TestObject();
        update.distributionId(testObject.distributionId());
        Assert.assertTrue(dataStore.load(update));
        update.type ="btype";
        Assert.assertTrue(dataStore.update(update));
        Assert.assertEquals(update.revision(),2L);
        TestObject load = new TestObject();
        load.distributionId(testObject.distributionId());
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(load.revision(),2L);
        Assert.assertEquals(load.type,"btype");
        Assert.assertEquals(load.name,"aname");

        TestObject failed = new TestObject();
        failed.distributionId(testObject.distributionId());
        Assert.assertFalse(dataStore.update(failed));

        TestObject load1 = new TestObject();
        load1.distributionId(testObject.distributionId());
        Assert.assertTrue(dataStore.load(load1));
        Assert.assertEquals(load1.revision(),2L);
        Assert.assertEquals(load1.type,"btype");
        Assert.assertEquals(load1.name,"aname");


        TestObject load2 = new TestObject();
        load2.distributionId(testObject.distributionId());
        Assert.assertTrue(index.load(load2));
        Assert.assertEquals(load2.revision(),2L);
        Assert.assertEquals(load2.type,"btype");
        Assert.assertEquals(load2.name,"aname");

    }

}
