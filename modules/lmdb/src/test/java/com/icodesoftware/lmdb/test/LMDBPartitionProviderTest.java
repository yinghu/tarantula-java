package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;

import com.icodesoftware.lmdb.partition.LMDBPartitionProvider;

import com.icodesoftware.util.SnowflakeKey;
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
    public void createTest(){
        DataStore dataStore = lmdbPartitionProvider.createDataStore("create_users");
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
            DataStore badName = lmdbPartitionProvider.createDataStore("x#users");
            badName.create(new TestObject("bad", "bad"));
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);
    }

    @Test(groups = { "LMDBPartitionProviderTest" })
    public void createIfAbsentTest(){
        DataStore dataStore = lmdbPartitionProvider.createDataStore("if_users");
        Assert.assertNotNull(dataStore);
        long id = 1000;
        TestObject testObject = new TestObject("type","name");
        testObject.distributionId(id);
        Assert.assertTrue(dataStore.createIfAbsent(testObject,false));
        Assert.assertEquals(testObject.revision(),1);
        TestObject load = new TestObject();
        load.distributionId(testObject.distributionId());

        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(testObject.name,load.name);
        Assert.assertEquals(testObject.type,load.type);
        Assert.assertEquals(load.revision(),1);

        TestObject  existed = new TestObject("type1","name1");
        existed.distributionId(id);
        Assert.assertFalse(dataStore.createIfAbsent(existed,true));
        Assert.assertEquals(existed.type,"type");
        Assert.assertEquals(existed.name,"name");
    }

    @Test(groups = { "LMDBPartitionProviderTest" })
    public void createEdgeTest(){
        DataStore dataStore = lmdbPartitionProvider.createDataStore("edge_users");
        Assert.assertNotNull(dataStore);
        TestObject testObject = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertEquals(testObject.revision(),1);
        testObject.ownerKey(SnowflakeKey.from(100));
        Assert.assertTrue(dataStore.createEdge(testObject,"friends"));
        long[] id = {0};
        dataStore.backup().forEachEdgeKey(SnowflakeKey.from(100),"friends",(k,v)->{
            id[0] = k.readLong();
            Assert.assertEquals(id[0],testObject.distributionId());
            return true;
        });
        Assert.assertTrue(id[0]>0);
        TestObject load = new TestObject();
        load.distributionId(id[0]);
        Assert.assertTrue(dataStore.load(load));

        Assert.assertTrue(dataStore.deleteEdge(SnowflakeKey.from(100),"friends"));
         
        //TestObject load = new TestObject();
        //load.distributionId(testObject.distributionId());

        //Assert.assertTrue(dataStore.load(load));
        //Assert.assertEquals(testObject.name,load.name);
        //Assert.assertEquals(testObject.type,load.type);
        //Assert.assertEquals(load.revision(),1);

    }


}
