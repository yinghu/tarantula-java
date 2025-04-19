package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SmokeNativeTest extends LMDBHook{


    @Test(groups = { "native data store" })
    public void startTest(){
        Exception exception = null;
        try{
            DataStore data = lmdbDataStoreProvider.createDataStore("test");
            Assert.assertEquals(data.scope(), Distributable.DATA_SCOPE);
            DataStore access = lmdbDataStoreProvider.createAccessIndexDataStore("test");
            Assert.assertEquals(access.scope(), Distributable.INTEGRATION_SCOPE);
            DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore("test");
            Assert.assertEquals(index.scope(), Distributable.INDEX_SCOPE);
            DataStore log = lmdbDataStoreProvider.createLogDataStore("test");
            Assert.assertEquals(log.scope(), Distributable.LOG_SCOPE);
            DataStore local = lmdbDataStoreProvider.createLocalDataStore("test");
            Assert.assertEquals(local.scope(), Distributable.LOCAL_SCOPE);
        }
        catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
    }

    @Test(groups = { "native data store" })
    public void createTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("create_test_object");
        TestObject testObject = new TestObject("atype","aname");
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertTrue(testObject.distributionId()>0);
        Assert.assertEquals(count(dataStore),1);
    }

    @Test(groups = { "native data store" })
    public void createIfAbsentTest(){
        DataStore dataStore = lmdbDataStoreProvider.createAccessIndexDataStore("test_index");
        TestAccessIndex testObject = new TestAccessIndex("tester");
        testObject.ownerKey(SnowflakeKey.from(100));
        Assert.assertTrue(dataStore.createIfAbsent(testObject,false));
        Assert.assertFalse(dataStore.createIfAbsent(testObject,false));
        Assert.assertEquals(count(dataStore),1);
    }

    @Test(groups = { "native data store" })
    public void createEdgeTest(){
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("create_edge_test_object");
        TestObject testObject = new TestObject("atype","aname");
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertTrue(testObject.distributionId()>0);
        Assert.assertEquals(count(dataStore),1);
        Assert.assertFalse(dataStore.createEdge(testObject,"link"));
        testObject.ownerKey(SnowflakeKey.from(100));
        testObject.onEdge(true);
        Assert.assertTrue(dataStore.createEdge(testObject,"link"));
        Assert.assertEquals(count(dataStore,SnowflakeKey.from(100),"link"),1);
    }

}
