package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.util.SnowflakeIdGenerator;
import com.icodesoftware.util.TimeUtil;
import org.lmdbjava.*;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class LMDBSmokeTest {


    private String dir = "target/smoke";
    private long mapSize = 1_048_576L;

    long offset = 1_000_000_000_000l;


    private int maxStores = 100;
    private int maxReader = 100;
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

    @Test(groups = { "LMDBSmoke" })
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
        }));
        c.close();
        Assert.assertEquals(ct[0],10);
    }
    @Test(groups = { "LMDBSmoke" })
    public void keyTest() {
        Dbi<ByteBuffer> dbi = env.openDbi("tarantula_int_key", DbiFlags.MDB_CREATE);
        Txn<ByteBuffer> txn = env.txnWrite();
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        ByteBuffer value = ByteBuffer.allocateDirect(env.getMaxKeySize());
        for(long k=11 ; k<20;k++){
            key.putLong(k).flip();
            value.putLong(k).flip();
            dbi.put(txn,key,value);
            key.clear();
            value.clear();
        }
        for(long k=1 ; k<11;k++){
            key.putLong(k).flip();
            value.putLong(k).flip();
            dbi.put(txn,key,value);
            key.clear();
            value.clear();
        }
        CursorIterable<ByteBuffer> c = dbi.iterate(txn, KeyRange.all());
        long[] k = {1};
        c.iterator().forEachRemaining((kv->{
            Assert.assertEquals(kv.key().getLong(),k[0]++);
        }));
       txn.commit();
       txn.close();
       dbi.close();
       for(int i=1;i<20;i++){
           long[] r = range(i);
           Assert.assertEquals(r[1]-r[0],offset-1);
           //System.out.println("Range ["+i+"]"+r[0]+"-"+r[1]);
       }
    }

    private long[] range(int section){
        long end = offset*section;
        long start = end-offset;
        return new long[]{start,end-1};
    }

    @Test(groups = { "LMDBSmoke" })
    public void snowflakeTest() {
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(99, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        Dbi<ByteBuffer> dbi = env.openDbi("tarantula_snow_flake", DbiFlags.MDB_CREATE);
        Txn<ByteBuffer> txn = env.txnWrite();
        long k =  snowflakeIdGenerator.snowflakeId();
        long v =  snowflakeIdGenerator.snowflakeId();
        //System.out.println(snowflakeIdGenerator.fromSnowflakeId(v)[2]);
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        Recoverable.DataBuffer kp = BufferProxy.buffer(key);
        //key.order(ByteOrder.LITTLE_ENDIAN);
        kp.writeLong(k);
        key.flip();
        ByteBuffer value = ByteBuffer.allocateDirect(env.getMaxKeySize());
        Recoverable.DataBuffer vp = BufferProxy.buffer(value);
        //value.order(ByteOrder.LITTLE_ENDIAN);
        vp.writeLong(v);
        value.flip();
        dbi.put(txn,key,value);
        //txn.commit();
        //txn.close();
        key.rewind();
        value.clear();
        //Txn<ByteBuffer> read = env.txnRead();
        if(dbi.get(txn,key)!=null){
            txn.val().order(ByteOrder.LITTLE_ENDIAN);
            Recoverable.DataBuffer p = BufferProxy.buffer(txn.val());
            long vx = p.readLong();
            long[] v1 = snowflakeIdGenerator.fromSnowflakeId(v);
            long[] v2 = snowflakeIdGenerator.fromSnowflakeId(vx);
            Assert.assertEquals(v1[0],v2[0]);
            Assert.assertEquals(v1[1],v2[1]);
            Assert.assertEquals(v1[2],v2[2]);
            //System.out.println(snowflakeIdGenerator.fromSnowflakeId(vx)[1]);
        }
    }


}
