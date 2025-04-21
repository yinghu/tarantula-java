package com.icodesoftware.lmdb.test;

import com.beust.ah.A;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.EnvSetting;
import com.icodesoftware.lmdb.LocalMetadata;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;



public class CreateTest extends LMDBHook{

    @Test(groups = { "native data store" })
    public void createTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_create_test_object");
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



        testMapStoreListener.transactionLogManager.onRecovering(LocalMetadata.metadata(Distributable.DATA_SCOPE, dataStore.name(),null),testObject.key(),(k,v)->{
            TestObject tox = new TestObject();
            Recoverable.DataHeader h = v.readHeader();
            Assert.assertEquals(h.factoryId(),testObject.getFactoryId());
            Assert.assertEquals(h.classId(),testObject.getClassId());
            Assert.assertEquals(h.revision(),testObject.revision());
            tox.read(v);
            Assert.assertEquals(tox.name,testObject.name);
            Assert.assertEquals(tox.type,testObject.type);
            return true;
        });

        testMapStoreListener.transactionLogManager.onRecovering(LocalMetadata.metadata(Distributable.DATA_SCOPE, dataStore.name(),"link"),SnowflakeKey.from(100),(k,v)->{
            TestObject tox = new TestObject();
            Recoverable.DataHeader h = v.readHeader();
            Assert.assertEquals(h.factoryId(),testObject.getFactoryId());
            Assert.assertEquals(h.classId(),testObject.getClassId());
            Assert.assertEquals(h.revision(),testObject.revision());
            tox.read(v);
            Assert.assertEquals(tox.name,testObject.name);
            Assert.assertEquals(tox.type,testObject.type);
            return true;
        });
    }

}
