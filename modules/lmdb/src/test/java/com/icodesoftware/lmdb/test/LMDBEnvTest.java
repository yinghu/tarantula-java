package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.*;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.FileUtil;
import com.icodesoftware.util.SnowflakeKey;
import org.lmdbjava.*;
import org.testng.Assert;

import org.testng.annotations.Test;

import java.io.File;
import java.nio.ByteBuffer;

import java.util.List;


public class LMDBEnvTest extends LMDBHook{


    @Test(groups = { "LMDBRecovery" })
    public void testCreate(){
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        long ownerId = localDistributionIdGenerator.id();
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_env_user");
        TestObject testObject = new TestObject();
        testObject.name = "test";
        testObject.type = "user";
        testObject.onEdge(true);
        testObject.label("name_index");
        testObject.ownerKey(SnowflakeKey.from(ownerId));
        Assert.assertTrue(dataStore.create(testObject));
        Assert.assertTrue(dataStore.createEdge(testObject,"type_index"));
        Assert.assertTrue(dataStore.createEdge(testObject,"key_index"));


        dataStore.backup().drop(false);
        Assert.assertFalse(dataStore.backup().get(testObject.key(),(keyBuffer, dataBuffer) ->true));
        LMDBEnv env = lmdbDataStoreProvider.env(Distributable.DATA_SCOPE);
        Txn<ByteBuffer> read = env.txnRead();
        LocalEdgeDataStore edgeDataStore = env.localEdgeDataStore("test_env_user","name_index",read);
        try(read){
            Recoverable.DataBuffer key = BufferProxy.buffer(8,true);
            key.writeLong(ownerId);
            int[] ct={0};
            edgeDataStore.onEdge(read,key.flip(),(k,v)->{
                //Assert.assertEquals(k.readLong(),testObject.distributionId());
                ct[0]++;
                return true;
            });
            Assert.assertEquals(ct[0],0);
        }
        File snapshot = FileUtil.createFileIfNotExisted(lmdbDataStoreProvider.baseDir()+"/backup");
        lmdbDataStoreProvider.env(Distributable.INDEX_SCOPE).copy(snapshot);
        EnvFlags[] flags = new EnvFlags[]{EnvFlags.MDB_NOSYNC,EnvFlags.MDB_RDONLY_ENV};
        Env<ByteBuffer> lmdb = Env.create().setMapSize(EnvSetting.toBytesFromMb(1)).setMaxDbs(1024).setMaxReaders(10).open(snapshot,flags);
        try(lmdb){
            Dbi<ByteBuffer> dbi = lmdb.openDbi(TransactionLogManager.DATA_PREFIX_I+"test_env_user",DbiFlags.MDB_CREATE);
            Cursor<ByteBuffer> cursor = dbi.openCursor(lmdb.txnRead());
            try(cursor){
                Assert.assertTrue(cursor.first());
                ByteBuffer key = cursor.key();
                ByteBuffer value = cursor.val();
                dataStore.backup().set((k,v)->{
                    BufferUtil.copy(key,k);
                    BufferUtil.copy(value,v);
                    return true;
                });
            }
        }
        Assert.assertTrue(dataStore.backup().get(testObject.key(),(keyBuffer, dataBuffer) ->true));
    }



}
