package com.icodesoftware.lmdb.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.service.AccessIndexService;
import org.junit.AfterClass;
import org.junit.rules.TemporaryFolder;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.lmdbjava.DbiFlags.MDB_CREATE;


public class LMDBSmokeTest {

    LMDBDataStoreProvider lmdbDataStoreProvider;
    @BeforeClass
    public void setUp() throws Exception{
        System.out.println("startup..");
        lmdbDataStoreProvider = new LMDBDataStoreProvider();
        lmdbDataStoreProvider.start();
    }
    @AfterTest
    public void tearDown() throws Exception{
       System.out.println("tearDown");
       lmdbDataStoreProvider.shutdown();
    }

    @Test(groups = { "LMDB" })
    public void smokeTest() {
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME+"1");

        //ds.createIfAbsent()
    }
}
