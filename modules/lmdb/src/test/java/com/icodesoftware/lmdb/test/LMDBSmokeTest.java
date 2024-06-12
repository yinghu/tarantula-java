package com.icodesoftware.lmdb.test;

import com.icodesoftware.lmdb.EnvSetting;
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


    private String dir = "target/lmdb/smoke";
    private long mapSize = EnvSetting.toBytesFromMb(1);

    private int maxStores = 100;
    private int maxReader = 100;
    private Env<ByteBuffer> env;
    @BeforeClass
    public void setUp() throws Exception{
        EnvFlags[] flags = new EnvFlags[]{EnvFlags.MDB_NOSYNC};
        Path path = Paths.get(dir);
        if(!Files.exists(path)) Files.createDirectories(path);
        env = Env.create().setMapSize(mapSize).setMaxDbs(maxStores).setMaxReaders(maxReader).open(path.toFile(),flags);
    }
    @AfterTest
    public void tearDown() throws Exception{
        env.close();
    }

    @Test(groups = { "LMDBSmoke" })
    public void keyEdgeTest() {
        Dbi<ByteBuffer> dbi = env.openDbi("tarantula_data_edge", DbiFlags.MDB_CREATE,DbiFlags.MDB_DUPSORT);
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.putLong(100).flip();
        ByteBuffer key1 = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key1.putLong(200).flip();

        ByteBuffer edge = ByteBuffer.allocateDirect(env.getMaxKeySize());
        try(Txn<ByteBuffer> txn = env.txnWrite()){
            for(int i=0;i<10;i++){
                edge.clear().putLong(i).flip();
                Assert.assertTrue(dbi.put(txn,key,edge,PutFlags.MDB_NODUPDATA));
                Assert.assertTrue(dbi.put(txn,key1,edge.rewind(),PutFlags.MDB_NODUPDATA));
            }
            for(int i=0;i<10;i++){
                edge.clear().putLong(i).flip();
                Assert.assertFalse(dbi.put(txn,key.rewind(),edge,PutFlags.MDB_NODUPDATA));
                Assert.assertFalse(dbi.put(txn,key1,edge.rewind(),PutFlags.MDB_NODUPDATA));
            }
            for(int i=0;i<10;i++){
                edge.clear().putLong(i).flip();
                Assert.assertFalse(dbi.put(txn,key.rewind(),edge,PutFlags.MDB_NODUPDATA));
                Assert.assertFalse(dbi.put(txn,key1,edge.rewind(),PutFlags.MDB_NODUPDATA));
            }
            txn.commit();
        }
        try(Txn<ByteBuffer> read = env.txnRead()){
            //key.rewind();
            try(Cursor<ByteBuffer> c = dbi.openCursor(read)){
                int[] ct ={0};
                c.get(key.rewind(),GetOp.MDB_SET);
                if(c.seek(SeekOp.MDB_FIRST_DUP)) ct[0]++;
                while (c.seek(SeekOp.MDB_NEXT_DUP)){
                    ct[0]++;
                }
                Assert.assertEquals(ct[0],10);
            }
        }
        try(Txn<ByteBuffer> txn = env.txnWrite()){
            for(int i=0;i<10;i++){
                key.rewind();
                edge.clear().putLong(i).flip();
                Assert.assertTrue(dbi.delete(txn,key,edge));
            }
            txn.commit();
        }
        try(Txn<ByteBuffer> read = env.txnRead()){
            try(Cursor<ByteBuffer> c = dbi.openCursor(read)){
                int[] ct ={0};
                if(c.get(key.rewind(),GetOp.MDB_SET)){
                    if(c.seek(SeekOp.MDB_FIRST_DUP)) ct[0]++;
                    while (c.seek(SeekOp.MDB_NEXT_DUP)){
                        ct[0]++;
                    }
                }
                Assert.assertEquals(ct[0],0);
            }
        }
        try(Txn<ByteBuffer> read = env.txnRead()){
            try(Cursor<ByteBuffer> c = dbi.openCursor(read)){
                int[] ct ={0};
                if(c.get(key1.rewind(),GetOp.MDB_SET)){
                    if(c.seek(SeekOp.MDB_FIRST_DUP)) ct[0]++;
                    while (c.seek(SeekOp.MDB_NEXT_DUP)){
                        ct[0]++;
                    }
                }
                Assert.assertEquals(ct[0],10);
            }
        }
        try(Txn<ByteBuffer> read = env.txnRead()){
            key1.rewind();
            try(Cursor<ByteBuffer> cursor = dbi.openCursor(read)){
                Assert.assertTrue(cursor.get(key1,GetOp.MDB_SET));
                Assert.assertEquals(cursor.count(),10);
                cursor.seek(SeekOp.MDB_FIRST_DUP);
                int[] ct={1};
                while (cursor.seek(SeekOp.MDB_NEXT_DUP)){
                    ct[0]++;
                }
                Assert.assertEquals(ct[0],10);
            }
        }
    }
    @Test(groups = { "LMDBSmoke" })
    public void keyValueTest() {
        Dbi<ByteBuffer> dbi = env.openDbi("tarantula_data_value", DbiFlags.MDB_CREATE);
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
            ByteBuffer value = ByteBuffer.allocateDirect(env.getMaxKeySize());
            int delta = 100;
            for (int k = 1; k < 11; k++) {
                key.putLong(k).flip();
                value.putLong(k + delta).flip();
                dbi.put(txn, key, value);
                key.clear();
                value.clear();
            }
            delta = 1000;
            for (long k = 1; k < 11; k++) {
                key.putLong(k).flip();
                value.putLong(k + delta).flip();
                dbi.put(txn, key, value);
                key.clear();
                value.clear();
            }
            txn.commit();
        }
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
            for (long k = 1; k < 11; k++) {
                key.putLong(k).flip();
                dbi.get(txn,key);
                Assert.assertEquals(txn.val().getLong(),k+1000);
                key.clear();
            }
        }
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            int[] ct={0};
            dbi.iterate(txn).iterator().forEachRemaining(kv->{
                ct[0]++;
            });
            Assert.assertEquals(ct[0],10);
        }
    }

    @Test(groups = { "LMDBSmoke" })
    public void cursorTest() {
        Dbi<ByteBuffer> dbi = env.openDbi("tarantula_data_cursor", DbiFlags.MDB_CREATE);
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
            ByteBuffer value = ByteBuffer.allocateDirect(env.getMaxKeySize());
            int delta = 100;
            for (int k = 1; k < 11; k++) {
                key.putLong(k).flip();
                value.putLong(k + delta).flip();
                dbi.put(txn, key, value);
                key.clear();
                value.clear();
            }
            delta = 1000;
            for (long k = 1; k < 11; k++) {
                key.putLong(k).flip();
                value.putLong(k + delta).flip();
                dbi.put(txn, key, value);
                key.clear();
                value.clear();
            }
            txn.commit();
        }
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            try(Cursor<ByteBuffer> cursor = dbi.openCursor(txn)){
                while (cursor.next()){
                    if(cursor.key().getLong()==1) cursor.delete();
                }
            }
            txn.commit();
        }
        try (Txn<ByteBuffer> txn = env.txnRead();Cursor<ByteBuffer> cursor = dbi.openCursor(txn)) {
            int[] ct={0};
            while (cursor.next()){
                ct[0]++;
            }
            Assert.assertEquals(ct[0],9);
        }
    }

    @Test(groups = { "LMDBSmoke" })
    public void txnNestedTest() {
        ByteBuffer key = ByteBuffer.allocateDirect(env.getMaxKeySize());
        key.putLong(100).flip();
        ByteBuffer value = ByteBuffer.allocateDirect(700);
        value.putLong(100).flip();
        Exception exception = null;
        try(final Txn<ByteBuffer> c = env.txn(null)){
            Dbi dbi1 = env.openDbi(c,"test1".getBytes(),null,false,DbiFlags.MDB_CREATE);
            try(final Txn<ByteBuffer> w = env.txn(c)){
                dbi1.put(w,key,value);
                w.commit();
            }
            Dbi dbi2 = env.openDbi(c,"test2".getBytes(),null,false,DbiFlags.MDB_CREATE);
            try(final Txn<ByteBuffer> w = env.txn(c)){
                dbi2.put(w,key.rewind(),value.rewind());
                w.commit();
            }
            c.commit();
        }
        catch (Exception ex){
            exception = ex;
        }
        finally {
            env.sync(true);
        }
        Assert.assertNull(exception);
        Dbi dbi1 = env.openDbi("test1",DbiFlags.MDB_CREATE);
        Dbi dbi2 = env.openDbi("test2",DbiFlags.MDB_CREATE);
        try(final Txn<ByteBuffer> r = env.txnRead()){
            Assert.assertTrue(dbi1.get(r,key.rewind())!=null);
            Assert.assertEquals(r.val().getLong(),100);
        }
        try(final Txn<ByteBuffer> r = env.txnRead()){
            Assert.assertTrue(dbi2.get(r,key.rewind())!=null);
            Assert.assertEquals(r.val().getLong(),100);
        }
    }



}
