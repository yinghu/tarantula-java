package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.util.NaturalKey;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;


public class LMDBDataStoreTest {
    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    LMDBDataStoreProvider lmdbDataStoreProvider;
    @BeforeClass
    public void setUp() throws Exception{
        lmdbDataStoreProvider = new LMDBDataStoreProvider();
        //lmdbDataStoreProvider.configure(new HashMap<>(){{
            //put("dir","target/lmdb");
        //}});
        lmdbDataStoreProvider.start();
        lmdbDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,new TestMapStoreListener(lmdbDataStoreProvider));
    }
    @AfterTest
    public void tearDown() throws Exception{
       lmdbDataStoreProvider.shutdown();
    }

    @Test(groups = { "LMDB" })
    public void createIfAbsentTest() {
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME+"1");
        String key = "a100";
        TestAccessIndex created = new TestAccessIndex(key);
        Assert.assertTrue(ds.createIfAbsent(created,false));
        TestAccessIndex not_created = new TestAccessIndex(key);
        Assert.assertFalse(ds.createIfAbsent(not_created,false));
        Assert.assertTrue(ds.load(not_created));
        Assert.assertTrue(ds.update(not_created));
        Assert.assertTrue(ds.update(not_created));
        Assert.assertTrue(ds.update(not_created));
        Assert.assertTrue(ds.load(not_created));
        Assert.assertEquals(not_created.revision(),Long.MIN_VALUE+3);

        Assert.assertTrue(ds.load(new NaturalKey(key), dataBuffer -> {
            TestAccessIndex testAccessIndex = new TestAccessIndex();
            Recoverable.DataHeader header = dataBuffer.readHeader();
            testAccessIndex.read(dataBuffer);
            Assert.assertEquals(header.factoryId(),testAccessIndex.getFactoryId());
            return true;
        }));
    }
    @Test(groups = { "LMDB" })
    public void createWithEdgeTest() {
        DataStore ds = lmdbDataStoreProvider.createDataStore("user");
        String ownerId1 = "s10000";
        String ownerId2 = "s20000";
        List<TestUser> empty = ds.list(new TestUserQuery(ownerId1));
        Assert.assertTrue(empty.size()==0);
        for(int i=0;i<10;i++) {
            TestUser testUser = new TestUser("user"+i,ownerId1);
            Assert.assertTrue(ds.create(testUser));
        }
        for(int i=0;i<100;i++) {
            TestUser testUser = new TestUser("user"+i,ownerId2);
            Assert.assertTrue(ds.create(testUser));
        }
        int[] c={0};
        ds.backup().list((v)->{
            c[0]++;
            return true;
        });
        Assert.assertEquals(c[0],110);

        c[0]=0;
        ds.list(new TestUserQuery(ownerId1),(t)->{
            c[0]++;
            return true;
        });
        Assert.assertEquals(c[0],10);
        List<TestUser> ulist = ds.list(new TestUserQuery(ownerId2));
        Assert.assertEquals(ulist.size(),100);
        ulist.forEach(u->{
            Assert.assertTrue(ds.load(u));
        });

        List<TestUser> zerolist = ds.list(new TestUserQuery("s1200"));
        Assert.assertEquals(zerolist.size(),0);

        DataStore dsx = lmdbDataStoreProvider.createDataStore("user_backup");
        int[] ct = {0};


        dsx.backup().list((buffer)->{
            //ct[0]++;
            Recoverable.DataHeader h = buffer.readHeader();
            if(h.classId()==10){
                TestUser testUser = new TestUser();
                testUser.read(buffer);
                //System.out.println(testUser.oid());
                ct[0]++;
            }
            return true;
        });
        Assert.assertEquals(ct[0],110);
    }
    //@Test(groups = { "LMDB" })
    public void createEdgeTest() {
        DataStore ds = lmdbDataStoreProvider.createDataStore("userex");
        String ownerId1 = "s10000";
        TestUser testUser = new TestUser("user",ownerId1);
        Assert.assertTrue(ds.create(testUser));
        Assert.assertTrue(ds.createEdge(testUser,"friends"));
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),1);
    }

    @Test(groups = { "LMDB" })
    public void createAssignedTest() {
        //DataStore ds = lmdbDataStoreProvider.createDataStore("users");
        String ownerId1 = "s10000";
        TestUserEx testUser = new TestUserEx("user",ownerId1);
        TestMapStoreListener mapStoreListener = new TestMapStoreListener(lmdbDataStoreProvider);
        ByteBuffer key = ByteBuffer.allocate(100);
        Recoverable.DataBuffer dataBuffer = new BufferProxy(key);
        mapStoreListener.assignKey(dataBuffer);
        key.flip();
        testUser.readKey(dataBuffer);
        Assert.assertNotNull(testUser.oid());
        key.clear();
        testUser.writeKey(dataBuffer);
        key.rewind();
        TestUserEx tc = new TestUserEx("",ownerId1);
        tc.readKey(dataBuffer);
        Assert.assertEquals(testUser.oid(),tc.oid());
        //Assert.assertTrue(ds.create(testUser));
        //Assert.assertTrue(ds.createEdge(testUser,"friends"));
        //Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),1);
    }

}
