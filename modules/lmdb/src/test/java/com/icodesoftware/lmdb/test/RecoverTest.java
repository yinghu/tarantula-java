package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;


public class RecoverTest extends LMDBHook{

    @Test(groups = { "native data store" })
    public void recoverTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_recover_test_object");

        TestObject testObject = new TestObject("atype","aname");
        testObject.ownerKey(SnowflakeKey.from(100));
        testObject.onEdge(true);
        testObject.label("link");
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertTrue(dataStore.createEdge(testObject,"provider"));
        Assert.assertEquals(testObject.revision(), EnvSetting.REVISION_START);
        Assert.assertTrue(testObject.distributionId()>0);
        Assert.assertTrue(dataStore.backup().unset((k,v)->{
            testObject.key().write(k);
            return true;
        }));
        Assert.assertTrue(dataStore.backup().unsetEdge("link",(k,v)->{
            testObject.ownerKey().write(k);
            testObject.key().write(v);
            return true;
        },false));

        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+dataStore.name());
        Assert.assertEquals(index.list(new TestObjectQuery(100,"link")).size(),1);
        Assert.assertEquals(dataStore.list(new TestObjectQuery(100,"link")).size(),1);

        TestObject update = new TestObject();
        update.distributionId(testObject.distributionId());
        index.load(update);
        update.type = "btype";
        Assert.assertTrue(index.update(update));

        TestObject load = new TestObject();
        load.distributionId(testObject.distributionId());


        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(load.revision(),2L);
        Assert.assertEquals(load.type,"btype");


    }

}
