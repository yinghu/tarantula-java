package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.*;
import com.icodesoftware.util.SnowflakeKey;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class CachedLMDBDataStoreTest extends LMDBHook{


    @Test(groups = { "LMDB" })
    public void testCreate(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            Assert.assertEquals(logs.size(),2);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_cmdb_user");
        TestUser user = new TestUser("test001",ownerId);
        Assert.assertTrue(dataStore.create(user));
        TestUser user1 = new TestUser("test002",ownerId);
        Assert.assertTrue(dataStore.create(user1));
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);

        //verify from index
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+"test_cmdb_user");
        List<TestUser> pending = new ArrayList<>();
        index.backup().forEachEdgeKey(SnowflakeKey.from(ownerId),TestUser.LABEL,(k,v)->{
            TestUser uid = new TestUser();
            uid.readKey(v);
            pending.add(uid);
            k.rewind();
            Assert.assertEquals(k.readLong(),ownerId);
            Assert.assertTrue(uid.distributionId()==user1.distributionId() || uid.distributionId()==user.distributionId());
            return true;
        });
        Assert.assertEquals(pending.size(),2);
        pending.forEach(u->{
            Assert.assertTrue(index.load(u));
        });
        index.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId),TestUser.LABEL,(k,v)->{
            Recoverable.DataHeader header = v.readHeader();
            Assert.assertEquals(header.factoryId(),user.getFactoryId());
            Assert.assertEquals(header.classId(),user.getClassId());
            TestUser u = new TestUser();
            Assert.assertTrue(u.readKey(k));
            Assert.assertTrue(u.read(v));
            return true;
        });
    }


    @Test(groups = { "LMDB" })
    public void testCreateIfAbsent() {
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_cmdb_access");
        long ownerId = localDistributionIdGenerator.id();
        int size = 5;
        for(int i=0;i<size;i++){
            TestAccessIndex testUser = new TestAccessIndex("user"+i);
            testUser.ownerKey(SnowflakeKey.from(ownerId));
            testUser.distributionId(localDistributionIdGenerator.id());
            Assert.assertTrue(ds.createIfAbsent(testUser,false));
            Assert.assertTrue(ds.createEdge(testUser,"friend"));
        }
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),5);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),5);
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.ACCESS_PREFIX_I+"test_cmdb_access");
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),5);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),5);

    }

    @Test(groups = { "LMDB" })
    public void testUpdate() {
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_cmdb_update");
        long ownerId = localDistributionIdGenerator.id();
        TestAccessIndex testUser = new TestAccessIndex("player1");
        int referenceId = testUser.referenceId();
        testUser.ownerKey(SnowflakeKey.from(ownerId));
        testUser.distributionId(localDistributionIdGenerator.id());
        Assert.assertTrue(ds.createIfAbsent(testUser,false));
        Assert.assertTrue(ds.createEdge(testUser,"friend"));
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),1);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),1);
        TestAccessIndex load = new TestAccessIndex("player1");
        Assert.assertTrue(ds.load(load));
        Assert.assertEquals(load.referenceId(),referenceId);
        load.referenceId = 100;
        Assert.assertTrue(ds.update(load));
        Assert.assertEquals(load.revision(),Long.MIN_VALUE+1);
        TestAccessIndex load1 = new TestAccessIndex("player1");
        Assert.assertTrue(ds.load(load1));
        Assert.assertEquals(load1.referenceId(),100);

        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.ACCESS_PREFIX_I+"test_cmdb_update");
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),1);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),1);
        TestAccessIndex load3 = new TestAccessIndex("player1");
        Assert.assertTrue(index.load(load3));
        Assert.assertEquals(load3.referenceId(),100);
        Assert.assertEquals(load3.revision(),Long.MIN_VALUE+1);

    }

    @Test(groups = { "LMDB" })
    public void testDelete() {
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_cmdb_delete");
        long ownerId = localDistributionIdGenerator.id();
        TestAccessIndex testUser = new TestAccessIndex("player1");
        int referenceId = testUser.referenceId();
        testUser.ownerKey(SnowflakeKey.from(ownerId));
        testUser.distributionId(localDistributionIdGenerator.id());
        Assert.assertTrue(ds.createIfAbsent(testUser,false));
        Assert.assertTrue(ds.createEdge(testUser,"friend"));
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),1);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),1);
        TestAccessIndex load = new TestAccessIndex("player1");
        Assert.assertTrue(ds.load(load));
        Assert.assertEquals(load.referenceId(),referenceId);
        load.referenceId = 100;
        Assert.assertTrue(ds.update(load));
        Assert.assertEquals(load.revision(),Long.MIN_VALUE+1);
        TestAccessIndex load1 = new TestAccessIndex("player1");
        Assert.assertTrue(ds.load(load1));
        Assert.assertEquals(load1.referenceId(),100);

        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.ACCESS_PREFIX_I+"test_cmdb_delete");
        Assert.assertTrue(ds.deleteEdge(SnowflakeKey.from(ownerId),testUser.key(),"friend"));
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),1);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),0);

        TestAccessIndex load3 = new TestAccessIndex("player1");
        Assert.assertTrue(ds.load(load3));
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),1);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),0);

        ds.deleteEdge(SnowflakeKey.from(ownerId),"access");

        TestAccessIndex load4 = new TestAccessIndex("player1");
        Assert.assertTrue(ds.load(load4));
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),0);

        ds.delete(load4);
        TestAccessIndex load5 = new TestAccessIndex("player1");
        Assert.assertFalse(index.load(load5));
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),0);

    }

    @Test(groups = { "LMDB" })
    public void testDeleteEdge() {
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_cmdb_delete_edge");
        long ownerId = localDistributionIdGenerator.id();
        int size = 5;
        for(int i=0;i<size;i++){
            TestAccessIndex testUser = new TestAccessIndex("user"+i);
            testUser.ownerKey(SnowflakeKey.from(ownerId));
            testUser.distributionId(localDistributionIdGenerator.id());
            Assert.assertTrue(ds.createIfAbsent(testUser,false));
            Assert.assertTrue(ds.createEdge(testUser,"friend"));
        }
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),5);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),5);
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.ACCESS_PREFIX_I+"test_cmdb_delete_edge");
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),5);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),5);

        TestAccessIndex testUser = new TestAccessIndex("user0");
        ds.deleteEdge(SnowflakeKey.from(ownerId),testUser.key(),"access");
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),4);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),5);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),4);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),5);

        TestAccessIndex testUser1 = new TestAccessIndex("user1");
        ds.deleteEdge(SnowflakeKey.from(ownerId),testUser1.key(),"friend");

        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),4);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),4);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),4);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),4);

        ds.deleteEdge(SnowflakeKey.from(ownerId),"access");
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),4);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),4);

        ds.deleteEdge(SnowflakeKey.from(ownerId),"friend");
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),0);


    }



}
