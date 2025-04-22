package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.LocalMetadata;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;


public class DeleteTest extends LMDBHook{

    @Test(groups = { "native data store" })
    public void deleteTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_delete_test_object");
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

        Assert.assertTrue(dataStore.deleteEdge(SnowflakeKey.from(100),testObject.key(),"link"));
        Assert.assertTrue(dataStore.delete(testObject));
        Assert.assertEquals(count(dataStore),0);
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),"link"),0);

        Assert.assertEquals(count(index),0);
        Assert.assertEquals(count(index,SnowflakeKey.from(100),"link"),0);

    }

}
