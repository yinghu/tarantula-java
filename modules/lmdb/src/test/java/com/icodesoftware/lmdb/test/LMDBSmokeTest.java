package com.icodesoftware.lmdb.test;


import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



public class LMDBSmokeTest {


    private String dir = "target/smoke";
    private long mapSize = 100_100_100;

    private int maxStores = 100;
    private int maxReader = 10;
    private Env<ByteBuffer> env;
    @BeforeClass
    public void setUp() throws Exception{
        Path path = Paths.get(dir);
        if(!Files.exists(path)) Files.createDirectories(path);
        env = Env.create().setMapSize(mapSize).setMaxDbs(maxStores).setMaxReaders(maxReader).open(path.toFile());
    }
    @AfterTest
    public void tearDown() throws Exception{
        env.close();
    }

    @Test(groups = { "LMDB" })
    public void smokeTest() {
        Dbi<ByteBuffer> dbi = env.openDbi("", DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);

    }
    @Test(groups = { "LMDB" })
    public void batchTest() {

    }
}
