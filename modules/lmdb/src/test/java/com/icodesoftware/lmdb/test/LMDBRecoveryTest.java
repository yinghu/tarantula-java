package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;
import com.icodesoftware.lmdb.*;
import com.icodesoftware.service.Batchable;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class LMDBRecoveryTest {

    LMDBDataStoreProvider lmdbDataStoreProvider;
    TestMapStoreListener testMapStoreListener;

    LocalDistributionIdGenerator localDistributionIdGenerator;
    @BeforeClass
    public void setUp() throws Exception{
        TestSetup.setUp();
        lmdbDataStoreProvider = TestSetup.lmdbDataStoreProvider;
        localDistributionIdGenerator = TestSetup.localDistributionIdGenerator;//new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        testMapStoreListener = TestSetup.testMapStoreListener;//new TestMapStoreListener(lmdbDataStoreProvider);
    }
    @AfterTest
    public void tearDown() throws Exception{

        //lmdbDataStoreProvider.shutdown();
    }
    @Test(groups = { "LMDBRecovery" })
    public void testCreate(){
        long ownerId = localDistributionIdGenerator.id();
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_user");
        Metadata metadata = new LocalMetadata(Distributable.DATA_SCOPE,"test_user");
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
            try(Recoverable.DataBufferPair kv = lmdbDataStoreProvider.dataBufferPair()){
                Recoverable.DataBuffer key  = kv.key();
                Recoverable.DataBuffer value = kv.value();
                key.writeLong(ownerId).flip();
                boolean recovery = testMapStoreListener.transactionLogManager.onRecovering(metadata,key,value);
                Assert.assertTrue(recovery);
                //if(recovery) {
                    //value.flip();
                    //Recoverable.DataHeader header = value.readHeader();
                    //TestObject tc = new TestObject();
                    //tc.read(value);
                    //System.out.println("TC : " + tc.type + " : " + tc.name + " : " + header.revision());
                //}
            }catch (Exception ex){

            }
            TestObject tx = new TestObject();
            tx.distributionId(ownerId);
            Assert.assertTrue(dataStore.load(tx));
            //System.out.println("Tx : "+tx.type+" : "+tx.name+" : "+tx.revision());
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
        //DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_user");
        Metadata metadata = new LocalMetadata(Distributable.DATA_SCOPE,"test_user");
        testMapStoreListener.verifier = (tid)->{
            //Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),2);
            ///System.out.println("TID : "+tid);
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
            try(Recoverable.DataBufferPair kv = lmdbDataStoreProvider.dataBufferPair()){
                Recoverable.DataBuffer key  = kv.key();
                Recoverable.DataBuffer value = kv.value();
                key.writeLong(ownerId).flip();
                if(testMapStoreListener.transactionLogManager.onRecovering(metadata,key,value)){
                    value.flip();
                    Recoverable.DataHeader header = value.readHeader();
                    TestObject tc = new TestObject();
                    tc.read(value);
                    //System.out.println("TTC : "+tc.type+" : "+tc.name+" : "+header.revision());
                }
            }catch (Exception ex){

            }
        };

        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
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

        Transaction read = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        read.execute(ctx->{
            DataStore dataStore = ctx.onDataStore("test_user");
            TestObject tx = new TestObject();
            tx.distributionId(ownerId);
            dataStore.load(tx);
            //System.out.println("TTx : "+tx.type+" : "+tx.name+" : "+tx.revision());
            return true;
        });

    }

}
