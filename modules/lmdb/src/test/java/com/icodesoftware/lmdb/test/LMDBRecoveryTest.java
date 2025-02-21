package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;
import com.icodesoftware.lmdb.*;

import com.icodesoftware.service.Metadata;

import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;


public class LMDBRecoveryTest extends LMDBHook{


    @Test(groups = { "LMDBRecovery" })
    public void testCreate(){
        long ownerId = localDistributionIdGenerator.id();
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_user");
        Metadata metadata = new LocalMetadata(Distributable.DATA_SCOPE,"test_user");
        testMapStoreListener.verifier = (tid)->{
            List<Transaction.Log> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
            try(Recoverable.DataBufferPair kv = lmdbDataStoreProvider.dataBufferPair()){
                Recoverable.DataBuffer key  = kv.key();
                Recoverable.DataBuffer value = kv.value();
                key.writeLong(ownerId).flip();
                boolean recovery = testMapStoreListener.transactionLogManager.onRecovering(metadata,key,value);
                Assert.assertTrue(recovery);
                if(recovery) {
                    value.flip();
                    Recoverable.DataHeader header = value.readHeader();
                    //System.out.println(header.revision());
                    TestObject tc = new TestObject();
                    tc.read(value);
                    Assert.assertEquals(header.classId(),tc.getClassId());
                    Assert.assertEquals(header.factoryId(),tc.getFactoryId());
                }
            }catch (Exception ex){

            }
        };
        TestObject user = new TestObject();
        user.name = "n001";
        user.type = "t001";
        user.distributionId(ownerId);
        Assert.assertTrue(dataStore.createIfAbsent(user,false));
        user.name = "n002";
        user.type = "t002";
        dataStore.update(user);
        user.name = "n003";
        user.type = "t003";
        dataStore.update(user);
    }

    @Test(groups = { "LMDBRecovery" })
    public void testCreateOnTransaction(){
        long ownerId = localDistributionIdGenerator.id();
        Metadata metadata = new LocalMetadata(Distributable.DATA_SCOPE,"test_user");
        testMapStoreListener.verifier = (tid)->{
            List<Transaction.Log> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
            try(Recoverable.DataBufferPair kv = lmdbDataStoreProvider.dataBufferPair()){
                Recoverable.DataBuffer key  = kv.key();
                Recoverable.DataBuffer value = kv.value();
                key.writeLong(ownerId).flip();
                boolean recovery = testMapStoreListener.transactionLogManager.onRecovering(metadata,key,value);
                key.rewind();
                Recoverable.DataBuffer dataBuffer = testMapStoreListener.transactionLogManager.get(metadata,key);
                key.rewind();
                Assert.assertTrue(testMapStoreListener.transactionLogManager.set(metadata,key,dataBuffer));
                Assert.assertNotNull(dataBuffer);
                Assert.assertTrue(recovery);
                key.rewind();
                value.clear();
                recovery = testMapStoreListener.transactionLogManager.onRecovering(metadata,key,value);
                if(recovery) {
                    value.flip();
                    Recoverable.DataHeader header = value.readHeader();
                    TestObject tc = new TestObject();
                    tc.read(value);
                    Assert.assertEquals(header.classId(),tc.getClassId());
                    Assert.assertEquals(header.factoryId(),tc.getFactoryId());
                }
            }catch (Exception ex){

            }
        };

        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore ds = ctx.onDataStore("test_user");
                TestObject user = new TestObject();
                user.name = "n001";
                user.type = "t001";
                user.distributionId(ownerId);
                Assert.assertTrue(ds.createIfAbsent(user,false));
                user.name = "n002";
                user.type = "t002";
                ds.update(user);
                user.name = "n003";
                user.type = "t003";
                ds.update(user);
                return true;
            });
        }

        try(Transaction read = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            read.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_user");
                TestObject tx = new TestObject();
                tx.distributionId(ownerId);
                Assert.assertTrue(dataStore.load(tx));
                return true;
            });
        }
    }



}
