package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.util.NaturalKey;

import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;


public class LMDBDataStoreTest {
    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    LMDBDataStoreProvider lmdbDataStoreProvider;
    TestMapStoreListener testMapStoreListener;

    LocalDistributionIdGenerator localDistributionIdGenerator;
    @BeforeClass
    public void setUp() throws Exception{

        lmdbDataStoreProvider = new LMDBDataStoreProvider();
        //lmdbDataStoreProvider.configure(new HashMap<>(){{
            //put("dir","target/lmdb");
        //}});
        localDistributionIdGenerator = new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        lmdbDataStoreProvider.registerDistributionIdGenerator(localDistributionIdGenerator);
        lmdbDataStoreProvider.start();
        testMapStoreListener = new TestMapStoreListener(lmdbDataStoreProvider);
        lmdbDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,testMapStoreListener);
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
        created.distributionId(localDistributionIdGenerator.id());
        Assert.assertTrue(ds.createIfAbsent(created,false));
        TestAccessIndex not_created = new TestAccessIndex(key);
        Assert.assertFalse(ds.createIfAbsent(not_created,false));
        Assert.assertTrue(ds.load(not_created));
        Assert.assertTrue(ds.update(not_created));
        Assert.assertTrue(ds.update(not_created));
        Assert.assertTrue(ds.update(not_created));
        Assert.assertTrue(ds.load(not_created));
        Assert.assertEquals(not_created.revision(),Long.MIN_VALUE+3);

        Assert.assertTrue(ds.load(new NaturalKey(key),(keybuffer,header,dataBuffer) -> {
            TestAccessIndex testAccessIndex = new TestAccessIndex();
            //Recoverable.DataHeader header = dataBuffer.readHeader();
            testAccessIndex.read(dataBuffer);
            Assert.assertEquals(testAccessIndex.distributionId(),created.distributionId());
            //System.out.println(testAccessIndex.distributionId());
            //long[] bits = testMapStoreListener.snowflakeIdGenerator.fromSnowflakeId(testAccessIndex.distributionId());
            //System.out.println(bits[0]);
            //System.out.println(bits[1]);
            //System.out.println(bits[2]);
            Assert.assertEquals(header.factoryId(),testAccessIndex.getFactoryId());
            return true;
        }));
    }

    @Test(groups = { "LMDB" })
    public void createWithEdgeTest() {
        DataStore ds = lmdbDataStoreProvider.createDataStore("user");
        long ownerId1 = 10000;
        long ownerId2 = 20000;
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
        ds.backup().list((k,h,v)->{
            c[0]++;
            return true;
        });
        //Assert.assertEquals(c[0],110);

        c[0]=0;
        ds.list(new TestUserQuery(ownerId1),(t)->{
            c[0]++;
            return true;
        });
        Assert.assertEquals(c[0],10);
        List<TestUser> ulist = ds.list(new TestUserQuery(ownerId2));
        Assert.assertEquals(ulist.size(),100);
        ulist.forEach(u->{
            //System.out.println(u.distributionId());
            TestUser ux = new TestUser();
            ux.distributionId(u.distributionId());
            Assert.assertTrue(ds.load(ux));
        });

        List<TestUser> zerolist = ds.list(new TestUserQuery(1200));
        Assert.assertEquals(zerolist.size(),0);

        DataStore dsx = lmdbDataStoreProvider.createDataStore("user_backup");
        int[] ct = {0};


        dsx.backup().list((key,h,buffer)->{
            //ct[0]++;
            //Recoverable.DataHeader h = buffer.readHeader();
            if(h.classId()==10){
                TestUser testUser = new TestUser();
                testUser.read(buffer);
                //System.out.println(testUser.oid());
                ct[0]++;
            }
            return true;
        });
        //Assert.assertEquals(ct[0],110);
    }
    @Test(groups = { "LMDB" })
    public void createEdgeTest() {
        DataStore ds = lmdbDataStoreProvider.createDataStore("test_use_c");
        long ownerId1 = 10000;
        TestUserEx testUser = new TestUserEx("user",ownerId1);
        Assert.assertTrue(ds.create(testUser));
        Assert.assertTrue(ds.createEdge(testUser,"friends"));
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),1);
    }

    @Test(groups = { "LMDB" })
    public void deleteWithEdgeTest() {
        DataStore ds = lmdbDataStoreProvider.createDataStore("test_use_d");
        long ownerId1 = 10000;

        TestUserEx testUser = new TestUserEx("user",ownerId1);
        Assert.assertTrue(ds.create(testUser));
        Assert.assertTrue(ds.createEdge(testUser,"friends"));
        TestUserEx testUser1 = new TestUserEx("user1",ownerId1);
        Assert.assertTrue(ds.create(testUser1));
        Assert.assertTrue(ds.createEdge(testUser1,"friends"));

        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,testUser.label())).size(),2);
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),2);
        Assert.assertTrue(ds.deleteEdge(testUser.ownerKey(),testUser.key(),"friends"));
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),1);
        Assert.assertTrue(ds.delete(testUser));
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,testUser.label())).size(),1);
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),1);
    }

    @Test(groups = { "LMDB" })
    public void createAssignedTest() {
        //DataStore ds = lmdbDataStoreProvider.createDataStore("users");
        long ownerId1 = 10000;
        TestUserEx testUser = new TestUserEx("user",ownerId1);
        TestMapStoreListener mapStoreListener = new TestMapStoreListener(lmdbDataStoreProvider);
        ByteBuffer key = ByteBuffer.allocate(100);
        Recoverable.DataBuffer dataBuffer = new BufferProxy(key);
        localDistributionIdGenerator.assign(dataBuffer);
        key.flip();
        testUser.readKey(dataBuffer);
        Assert.assertTrue(testUser.distributionId()>0);
        key.clear();
        testUser.writeKey(dataBuffer);
        key.rewind();
        TestUserEx tc = new TestUserEx("",ownerId1);
        tc.readKey(dataBuffer);
        Assert.assertTrue(tc.distributionId()>0);
        //Assert.assertTrue(ds.create(testUser));
        //Assert.assertTrue(ds.createEdge(testUser,"friends"));
        //Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),1);
    }
    @Test(groups = { "LMDB" })
    public void backupListTest() {
        DataStore ds = lmdbDataStoreProvider.createDataStore("batch_users");

        int batch = 100;
        for(int i=0;i<batch;i++){
            TestUserEx ex = new TestUserEx("BATCH"+i,199);
            Assert.assertTrue(ds.create(ex));
        }
        int[] ct ={0};
        ds.backup().list((k,h,v)->{
            TestUserEx ex = new TestUserEx(true);
            ex.readKey(k);
            ex.read(v);
            ct[0]++;
            Assert.assertTrue(ex.distributionId()>0);
            Assert.assertNotNull(ex.login());
            Assert.assertNotNull(ex.emailAddress());
            Assert.assertNotNull(ex.role());
            Assert.assertNotNull(ex.password());
            return true;
        });
        Assert.assertEquals(ct[0],batch);
        Assert.assertEquals(ds.count(),batch);
        //Assert.assertTrue(ds.create(testUser));
        //Assert.assertTrue(ds.createEdge(testUser,"friends"));
        //Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),1);
    }

}
