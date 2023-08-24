package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.util.NaturalKey;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;


public class LMDBDataStoreTest {

    LMDBDataStoreProvider lmdbDataStoreProvider;
    @BeforeClass
    public void setUp() throws Exception{
        lmdbDataStoreProvider = new LMDBDataStoreProvider();
        lmdbDataStoreProvider.configure(new HashMap<>(){{
            put("dir","target/lmdb");
        }});
        lmdbDataStoreProvider.start();
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
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME+"2");
        long ownerId1 = 10000;
        long ownerId2 = 20000;
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
        ulist.forEach(u-> Assert.assertTrue(ds.load(u)));

        List<TestUser> zerolist = ds.list(new TestUserQuery(1200));
        Assert.assertEquals(zerolist.size(),0);
    }


}
