package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LocalHeader;
import com.icodesoftware.lmdb.partition.LMDBPartitionProvider;

import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

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

        TestObject testObject1 = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject1));
        Assert.assertEquals(testObject1.revision(),1);
        testObject1.ownerKey(SnowflakeKey.from(100));

        TestObject testObject2 = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject2));
        Assert.assertEquals(testObject2.revision(),1);
        testObject2.ownerKey(SnowflakeKey.from(100));

        Assert.assertTrue(dataStore.createEdge(testObject,"friends"));
        Assert.assertTrue(dataStore.createEdge(testObject1,"friends"));
        Assert.assertTrue(dataStore.createEdge(testObject2,"friends"));

        List<TestObject> list = dataStore.list(new TestObjectQuery(100,"friends"));
        Assert.assertEquals(list.size(),3);

        dataStore.list(new TestObjectQuery(100,"friends"),t->{
            Assert.assertTrue(t.distributionId()>0);
            return true;
        });

        ArrayList<Long> ids = new ArrayList<>();
        dataStore.backup().forEachEdgeKey(SnowflakeKey.from(100),"friends",(k,v)->{
            ids.add(k.readLong());
            return true;
        });
        Assert.assertEquals(ids.size(),3);

        Assert.assertTrue(dataStore.deleteEdge(SnowflakeKey.from(100),SnowflakeKey.from(testObject1.distributionId()),"friends"));

        ids.clear();
        dataStore.backup().forEachEdgeKey(SnowflakeKey.from(100),"friends",(k,v)->{
            ids.add(k.readLong());
            return true;
        });
        Assert.assertEquals(ids.size(),2);


        Assert.assertTrue(dataStore.deleteEdge(SnowflakeKey.from(100),"friends"));
        ids.clear();
        dataStore.backup().forEachEdgeKey(SnowflakeKey.from(100),"friends",(k,v)->{
            ids.add(k.readLong());
            return true;
        });
        Assert.assertEquals(ids.size(),0);

    }

    @Test(groups = { "LMDBPartitionProviderTest" })
    public void setTest(){
        DataStore dataStore = lmdbPartitionProvider.createDataStore("set_users");
        Assert.assertNotNull(dataStore);
        TestObject testObject = new TestObject("type","name");
        testObject.distributionId(100);

        Assert.assertTrue(dataStore.backup().set((k,v)->{
            testObject.writeKey(k);
            v.writeHeader(new LocalHeader(testObject.revision(),testObject.getFactoryId(),testObject.getClassId()));
            testObject.write(v);
            return true;
        }));
        TestObject testObject3 = new TestObject();
        Assert.assertTrue(dataStore.backup().get(SnowflakeKey.from(testObject.distributionId()),(k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            testObject3.readKey(k);
            testObject3.read(v);
            testObject3.revision(h.revision());
            return true;
        }));
        Assert.assertEquals(testObject3.distributionId(),testObject.distributionId());
        Assert.assertEquals(testObject3.type,testObject.type);
        Assert.assertEquals(testObject3.name,testObject.name);

        Assert.assertTrue(dataStore.backup().unset((k,v)->{
            testObject3.writeKey(k);
            return true;
        }));
        Assert.assertFalse(dataStore.backup().get(SnowflakeKey.from(testObject.distributionId()),(k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            testObject3.readKey(k);
            testObject3.read(v);
            testObject3.revision(h.revision());
            return true;
        }));
    }

    @Test(groups = { "LMDBPartitionProviderTest" })
    public void setEdgeTest(){
        DataStore dataStore = lmdbPartitionProvider.createDataStore("set_edge_users");
        Assert.assertNotNull(dataStore);
        TestObject testObject = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertEquals(testObject.revision(),1);
        testObject.ownerKey(SnowflakeKey.from(100));

        TestObject testObject1 = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject1));
        Assert.assertEquals(testObject1.revision(),1);
        testObject1.ownerKey(SnowflakeKey.from(100));

        TestObject testObject2 = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject2));
        Assert.assertEquals(testObject2.revision(),1);
        testObject2.ownerKey(SnowflakeKey.from(100));

        Assert.assertTrue(dataStore.backup().setEdge("friends",(k,v)->{
            k.writeLong(100);
            v.writeLong(testObject.distributionId());
            return true;
        }));
        Assert.assertTrue(dataStore.backup().setEdge("friends",(k,v)->{
            k.writeLong(100);
            v.writeLong(testObject1.distributionId());
            return true;
        }));
        Assert.assertTrue(dataStore.backup().setEdge("friends",(k,v)->{
            k.writeLong(100);
            v.writeLong(testObject2.distributionId());
            return true;
        }));
        List<TestObject> list = dataStore.list(new TestObjectQuery(100,"friends"));
        Assert.assertEquals(list.size(),3);

        Assert.assertTrue(dataStore.backup().unsetEdge("friends",(k,v)->{
            k.writeLong(100);
            v.writeLong(testObject2.distributionId());
            return true;
        },false));
        list = dataStore.list(new TestObjectQuery(100,"friends"));
        Assert.assertEquals(list.size(),2);

        Assert.assertTrue(dataStore.backup().unsetEdge("friends",(k,v)->{
            k.writeLong(100);
            //v.writeLong(testObject2.distributionId());
            return true;
        },true));
        list = dataStore.list(new TestObjectQuery(100,"friends"));
        Assert.assertEquals(list.size(),0);
    }

    @Test(groups = { "LMDBPartitionProviderTest" })
    public void forEachTest(){
        DataStore dataStore = lmdbPartitionProvider.createDataStore("for_each_users");
        Assert.assertNotNull(dataStore);
        TestObject testObject = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertEquals(testObject.revision(),1);
        testObject.ownerKey(SnowflakeKey.from(100));

        TestObject testObject1 = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject1));
        Assert.assertEquals(testObject1.revision(),1);
        testObject1.ownerKey(SnowflakeKey.from(100));

        TestObject testObject2 = new TestObject("type","name");
        Assert.assertTrue(dataStore.create(testObject2));
        Assert.assertEquals(testObject2.revision(),1);
        testObject2.ownerKey(SnowflakeKey.from(100));

        Assert.assertTrue(dataStore.createEdge(testObject,"friends"));
        Assert.assertTrue(dataStore.createEdge(testObject1,"friends"));
        Assert.assertTrue(dataStore.createEdge(testObject2,"friends"));

        int[] ct={0};
        dataStore.backup().forEach((k,v)->{
            ct[0]++;
            return true;
        });
        Assert.assertEquals(ct[0],3);
        ct[0]= 0;
        dataStore.backup().forEachEdgeKey(SnowflakeKey.from(100),"friends",(k,v)->{
            ct[0]++;
            return true;
        });
        Assert.assertEquals(ct[0],3);
        dataStore.backup().drop(false);
        ct[0]=0;
        dataStore.backup().forEach((k,v)->{
            ct[0]++;
            return true;
        });
        Assert.assertEquals(ct[0],0);
        ct[0]= 0;
        dataStore.backup().forEachEdgeKey(SnowflakeKey.from(100),"friends",(k,v)->{
            ct[0]++;
            return true;
        });
        Assert.assertEquals(ct[0],0);
    }

}
