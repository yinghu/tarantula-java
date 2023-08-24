package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.AccessIndexService;

import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;



public class LMDBDataStoreMigrationTest {

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

    @Test(groups = { "LMDBMigration" })
    public void accessIndexHook() {
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME+"1");
        AccessIndex accessIndex = new AccessIndexTrack("test","BDS", SystemUtil.oid(),1);
        accessIndex.id(lmdbDataStoreProvider.nextId(ds.name()));
        Assert.assertTrue(ds.createIfAbsent(accessIndex,false));

        AccessIndex not_created = new AccessIndexTrack("test");
        Assert.assertFalse(ds.createIfAbsent(not_created,true));
        Assert.assertEquals(not_created.referenceId(),accessIndex.referenceId());
        Assert.assertEquals(not_created.id(),accessIndex.id());
        Assert.assertEquals(not_created.owner(),accessIndex.owner());
        Assert.assertEquals(not_created.bucket(),accessIndex.bucket());
        Assert.assertEquals(not_created.oid(),accessIndex.oid());

        AccessIndex load = new AccessIndexTrack("test");
        Assert.assertTrue(ds.load(load));

        Assert.assertEquals(load.referenceId(),accessIndex.referenceId());
        Assert.assertEquals(load.id(),accessIndex.id());
        Assert.assertEquals(load.owner(),accessIndex.owner());
        Assert.assertEquals(load.bucket(),accessIndex.bucket());
        Assert.assertEquals(load.oid(),accessIndex.oid());


    }


}
