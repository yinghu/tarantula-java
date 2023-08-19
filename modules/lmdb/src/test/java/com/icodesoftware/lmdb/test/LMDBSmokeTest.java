package com.icodesoftware.lmdb.test;


import org.lmdbjava.*;
import org.testng.Assert;
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
        Dbi<ByteBuffer> dbi = env.openDbi("tarantula_edge", DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
        Txn<ByteBuffer> txn = env.txnWrite();
        //Cursor<ByteBuffer> cursor = dbi.openCursor(txn);
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());

        ByteBuffer edge = ByteBuffer.allocateDirect(env.getMaxKeySize());
        for(int i=0;i<10;i++){
            key.put("key".getBytes()).flip();
            edge.put(("1edge"+i).getBytes()).flip();
            dbi.put(txn,key,edge,PutFlags.MDB_NODUPDATA);
            edge.clear();
            key.clear();
        }
        for(int i=0;i<10;i++){
            key.put("key2".getBytes()).flip();
            edge.put(("2edge"+i).getBytes()).flip();
            dbi.put(txn,key,edge,PutFlags.MDB_NODUPDATA);
            edge.clear();
            key.clear();
        }
        for(int i=0;i<10;i++){
            key.put("key2".getBytes()).flip();
            edge.put(("2edge"+i).getBytes()).flip();
            dbi.put(txn,key,edge,PutFlags.MDB_NODUPDATA);
            edge.clear();
            key.clear();
        }
        txn.commit();
        txn.close();
        Txn<ByteBuffer> read = env.txnRead();
        key.clear();
        key.put("key2".getBytes()).flip();
        CursorIterable<ByteBuffer> c = dbi.iterate(read, KeyRange.closed(key, key));
        int[] ct ={0};
        c.iterator().forEachRemaining((kv->{
            ct[0]++;
            //System.out.println(UTF_8.decode(kv.key()));
            //System.out.println(UTF_8.decode(kv.val()));
        }));
        c.close();
        Assert.assertEquals(ct[0],10);
    }
    @Test(groups = { "LMDB" })
    public void batchTest() {

    }
}
