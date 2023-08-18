package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.AccessIndexService;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.UUID;


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
    public void smokeTest() {
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME+"1");
        String key = "a100";
        TestAccessIndex testAccessIndex = new TestAccessIndex(key,"DBS", UUID.randomUUID().toString(),1);
        ds.createIfAbsent(testAccessIndex,false);
        TestAccessIndex load = new TestAccessIndex(key);
        Assert.assertTrue(ds.load(load));
        load.bucket("XDD");
        Assert.assertTrue(ds.update(load));
    }
    @Test(groups = { "LMDB" })
    public void batchTest() {
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME+"2");
        int batch =0;
        for(int i=0;i<100;i++) {
            String key = "px"+i;
            TestAccessIndex testAccessIndex = new TestAccessIndex(key, "DBS", UUID.randomUUID().toString(), 1);
            if(ds.createIfAbsent(testAccessIndex, false)) batch++;
        }
        Assert.assertEquals(batch,100);
        int[] keys ={0};
        ds.backup().list((k,v)->{
            keys[0]++;
            return true;
        });
        Assert.assertEquals(keys[0],100);
    }
}
