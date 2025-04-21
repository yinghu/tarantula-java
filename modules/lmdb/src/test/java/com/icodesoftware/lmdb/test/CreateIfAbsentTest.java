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


public class CreateIfAbsentTest extends LMDBHook{

    @Test(groups = { "native data store" })
    public void createTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_create_if_absent_test_object");
        TestAccessIndex testObject = new TestAccessIndex("tester");
        testObject.group = "abc";
        testObject.referenceId = 100;
        testObject.distributionId(localDistributionIdGenerator.id());
        testObject.ownerKey(SnowflakeKey.from(100));
        Assert.assertTrue(dataStore.createIfAbsent(testObject,false));
        Assert.assertEquals(testObject.revision(), EnvSetting.REVISION_START);
        Assert.assertEquals(count(dataStore),1);
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),testObject.label()),1);
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+dataStore.name());
        TestAccessIndex rep = new TestAccessIndex("tester");
        Assert.assertTrue(index.load(rep));
        Assert.assertEquals(count(index),1);
        Assert.assertEquals(count(index,SnowflakeKey.from(100),testObject.label()),1);

        int[] ct={0};
        testMapStoreListener.transactionLogManager.onRecovering(LocalMetadata.metadata(Distributable.DATA_SCOPE, dataStore.name(),null),testObject.key(),(k,v)->{
            TestAccessIndex tox = new TestAccessIndex();
            Recoverable.DataHeader h = v.readHeader();
            Assert.assertEquals(h.factoryId(),testObject.getFactoryId());
            Assert.assertEquals(h.classId(),testObject.getClassId());
            Assert.assertEquals(h.revision(),testObject.revision());
            tox.read(v);
            Assert.assertEquals(tox.group,testObject.group);
            Assert.assertEquals(tox.distributionId(),testObject.distributionId());
            Assert.assertEquals(tox.referenceId,testObject.referenceId);
            ct[0]++;
            return true;
        });
        Assert.assertEquals(ct[0],1);

        testMapStoreListener.transactionLogManager.onRecovering(LocalMetadata.metadata(Distributable.DATA_SCOPE, dataStore.name(),testObject.label()),SnowflakeKey.from(100),(k,v)->{
            TestAccessIndex tox = new TestAccessIndex();
            Recoverable.DataHeader h = v.readHeader();
            Assert.assertEquals(h.factoryId(),testObject.getFactoryId());
            Assert.assertEquals(h.classId(),testObject.getClassId());
            Assert.assertEquals(h.revision(),testObject.revision());
            tox.read(v);
            Assert.assertEquals(tox.group,testObject.group);
            Assert.assertEquals(tox.distributionId(),testObject.distributionId());
            Assert.assertEquals(tox.referenceId,testObject.referenceId);
            ct[0]++;
            return true;
        });

        Assert.assertEquals(ct[0],2);

        TestAccessIndex load = new TestAccessIndex("tester");
        Assert.assertFalse(dataStore.createIfAbsent(load,true));
        Assert.assertEquals(load.group,testObject.group);
        Assert.assertEquals(load.distributionId(),testObject.distributionId());
        Assert.assertEquals(load.referenceId,testObject.referenceId);
        Assert.assertEquals(load.revision(),testObject.revision());
    }

}
