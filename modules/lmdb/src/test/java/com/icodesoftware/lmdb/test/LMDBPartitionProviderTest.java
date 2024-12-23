package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;

import com.icodesoftware.lmdb.partition.LMDBPartitionProvider;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LMDBPartitionProviderTest {

    LMDBPartitionProvider lmdbPartitionProvider;

    @BeforeClass
    public void setUp() throws Exception{
        TestSetup.setUp();
        lmdbPartitionProvider = TestSetup.lmdbPartitionProvider;
    }

    @Test(groups = { "LMDBPartitionProviderTest" })
    public void initialTest(){
        DataStore dataStore = lmdbPartitionProvider.createDataStore("users");
        Assert.assertNotNull(dataStore);
        TestObject testObject = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertEquals(testObject.revision(),1);
        TestObject load = new TestObject();
        load.distributionId(testObject.distributionId());
        Assert.assertTrue(load.distributionId() >0);
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(testObject.name,load.name);
        Assert.assertEquals(testObject.type,load.type);
        Assert.assertEquals(load.revision(),1);
        testObject.type = "another";
        Assert.assertTrue(dataStore.update(testObject));
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(load.type,"another");
        Assert.assertEquals(load.revision(),2);
        Assert.assertTrue(dataStore.delete(load));
        Assert.assertFalse(dataStore.load(load));

    }
    @Test(groups = { "LMDBPartitionProviderTest" })
    public void namingTest() {
        Exception exception = null;
        try {
            DataStore badName = lmdbPartitionProvider.createDataStore("x-users");
            badName.create(new TestObject("bad", "bad"));
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            DataStore badName = lmdbPartitionProvider.createDataStore("x@users");
            badName.create(new TestObject("bad", "bad"));
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);

        exception = null;
        try {
            DataStore badName = lmdbPartitionProvider.createDataStore("x-users");
            badName.create(new TestObject("bad", "bad"));
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);
    }
}
