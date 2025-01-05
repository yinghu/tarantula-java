package com.icodesoftware.lmdb.test;

import com.icodesoftware.*;
import com.icodesoftware.lmdb.*;


import com.icodesoftware.service.Batchable;
import com.icodesoftware.service.Metadata;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.SnowflakeKey;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;


public class LMDBDataStoreTest extends LMDBHook{


    @Test(groups = { "LMDB" })
    public void testCreate(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_user");
                TestUser user = new TestUser("test001",ownerId);
                Assert.assertTrue(dataStore.create(user));
                TestUser user1 = new TestUser("test002",ownerId);
                Assert.assertTrue(dataStore.create(user1));
                return user.distributionId()>0 && user1.distributionId()>0;
            });
        }
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+"test_lmdb_user");
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_lmdb_user");
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);
        Assert.assertEquals(index.list(new TestUserQuery(ownerId)).size(),2);
        dataStore.list(new TestUserQuery(ownerId)).forEach(u->{
            TestUser ux = new TestUser();
            ux.distributionId(u.distributionId());
            Assert.assertTrue(index.load(ux));
        });
    }

    @Test(groups = { "LMDB" })
    public void testCreateIfAbsent() {
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        long ownerId = localDistributionIdGenerator.id();
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore ds = ctx.onDataStore("test_lmdb_access");
                int size = 5;
                for(int i=0;i<size;i++){
                    TestAccessIndex testUser = new TestAccessIndex("user"+i);
                    testUser.ownerKey(SnowflakeKey.from(ownerId));
                    testUser.distributionId(localDistributionIdGenerator.id());
                    Assert.assertTrue(ds.createIfAbsent(testUser,false));
                }
                return true;
            });
        }
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.ACCESS_PREFIX_I+"test_lmdb_access");
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),5);
    }

    @Test(groups = { "LMDB" })
    public void testUpdate() {
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        long ownerId = localDistributionIdGenerator.id();
        TestUser testUser = new TestUser();
        testUser.ownerKey(SnowflakeKey.from(ownerId));
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.DATA_PREFIX_I+"test_lmdb_update");
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore ds = ctx.onDataStore("test_lmdb_update");
                Assert.assertTrue(ds.create(testUser));
                return true;
            });
        }
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore ds = ctx.onDataStore("test_lmdb_update");
                TestUser update = new TestUser();
                update.distributionId(testUser.distributionId());
                Assert.assertTrue(ds.load(update));
                update.password("newpass");
                Assert.assertTrue(ds.update(update));
                return true;
            });
        }
        TestUser load = new TestUser();
        load.distributionId(testUser.distributionId());
        Assert.assertTrue(index.load(load));
        Assert.assertEquals(load.password,"newpass");
        Assert.assertEquals(load.revision(),Long.MIN_VALUE+1);

    }
    @Test(groups = { "LMDB" })
    public void testDelete() {
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_lmdb_delete");
        long ownerId = localDistributionIdGenerator.id();
        TestAccessIndex testUser = new TestAccessIndex("player1");
        long dis = localDistributionIdGenerator.id();
        testUser.ownerKey(SnowflakeKey.from(ownerId));
        testUser.distributionId(dis);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)) {
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete");
                Assert.assertTrue(dataStore.createIfAbsent(testUser, false));
                Assert.assertTrue(dataStore.createEdge(testUser, "friend"));
                return true;
            });
        };
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)) {
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete");
                Assert.assertTrue(dataStore.deleteEdge(SnowflakeKey.from(ownerId),testUser.key(),"friend"));
                return true;
            });
        };
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.ACCESS_PREFIX_I+"test_lmdb_delete");
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),1);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),0);

        TestAccessIndex load3 = new TestAccessIndex("player1");
        Assert.assertTrue(ds.load(load3));
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),1);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),0);

        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)) {
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete");
                Assert.assertTrue(dataStore.deleteEdge(SnowflakeKey.from(ownerId),testUser.key(),"access"));
                return true;
            });
        };

        TestAccessIndex load4 = new TestAccessIndex("player1");
        Assert.assertTrue(ds.load(load4));
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),0);

        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)) {
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete");
                Assert.assertTrue(dataStore.delete(load4));
                return true;
            });
        };
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
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_lmdb_delete_edge");
        long ownerId = localDistributionIdGenerator.id();
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete_edge");
                int size = 5;
                for(int i=0;i<size;i++){
                    TestAccessIndex testUser = new TestAccessIndex("user"+i);
                    testUser.ownerKey(SnowflakeKey.from(ownerId));
                    testUser.distributionId(localDistributionIdGenerator.id());
                    Assert.assertTrue(dataStore.createIfAbsent(testUser,false));
                    Assert.assertTrue(dataStore.createEdge(testUser,"friend"));
                }
                return true;
            });
        }
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),5);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),5);
        DataStore index = lmdbDataStoreProvider.createKeyIndexDataStore(TransactionLogManager.ACCESS_PREFIX_I+"test_lmdb_delete_edge");
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),5);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),5);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete_edge");
                TestAccessIndex testUser = new TestAccessIndex("user0");
                return dataStore.deleteEdge(SnowflakeKey.from(ownerId),testUser.key(),"access");
            });
        }
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),4);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),5);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),4);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),5);

        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete_edge");
                TestAccessIndex testUser1 = new TestAccessIndex("user1");
                return dataStore.deleteEdge(SnowflakeKey.from(ownerId),testUser1.key(),"friend");
            });
        }

        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),4);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),4);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),4);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),4);

        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete_edge");
                return dataStore.deleteEdge(SnowflakeKey.from(ownerId),"access");
            });
        }
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),4);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),4);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_lmdb_delete_edge");
                return dataStore.deleteEdge(SnowflakeKey.from(ownerId),"friend");
            });
        }
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"friend")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"access")).size(),0);
        Assert.assertEquals(index.list(new TestAccessQuery(ownerId,"friend")).size(),0);


    }


    @Test(groups = { "LMDB" })
    public void testCommitOnTransaction(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            Assert.assertEquals(logs.size(),4);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        TestUser user = new TestUser("test001",ownerId);
        TestUser user1 = new TestUser("test002",ownerId);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_user_committed");
                Assert.assertTrue(dataStore.create(user));
                Assert.assertTrue(dataStore.create(user1));
                return true;
            });
        }

        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_user_committed");
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);
        byte[] data = testMapStoreListener.transactionLogManager.loadFromCommitted(new LocalMetadata(Distributable.DATA_SCOPE,"test_user_committed"), SnowflakeKey.from(user.distributionId()).asBinary());
        Assert.assertNotNull(data);
        Recoverable.DataBuffer bufferProxy = BufferProxy.wrap(data);
        bufferProxy.readHeader();
        TestUser testUser = new TestUser();
        testUser.read(bufferProxy);
        Assert.assertEquals(testUser.login,"test001");
        Metadata metadata = new LocalMetadata(Distributable.DATA_SCOPE,"test_user_committed",TestUser.LABEL);
        List<Batchable.BatchData> batchable = testMapStoreListener.transactionLogManager.loadEdgeValueFromCommitted(metadata,SnowflakeKey.from(ownerId).asBinary());
        Assert.assertEquals(batchable.size(),2);
        //List<byte[]> kp = batchable.key();
        //List<byte[]> vp = batchable.data();
        for(int i=0;i<batchable.size();i++){
            TestUser tx = new TestUser();
            Batchable.BatchData batchData = batchable.get(i);
            tx.readKey(BufferProxy.wrap(batchData.key()));
            Recoverable.DataBuffer buffer = BufferProxy.wrap(batchData.value());
            buffer.readHeader();
            tx.read(buffer);
            Assert.assertTrue(tx.login().startsWith("test00"));
            Assert.assertTrue(tx.distributionId()>0);
        }
    }
    @Test(groups = { "LMDB" })
    public void testAbortOnTransaction(){
        long ownerId = localDistributionIdGenerator.id();
        boolean[] aborted={false};
        testMapStoreListener.abort = (tid)->{
            aborted[0]=true;
        };
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            boolean committed = transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_user_abort");
                TestUser user = new TestUser("test001",ownerId);
                Assert.assertTrue(dataStore.create(user));
                TestUser user1 = new TestUser("test002",ownerId);
                Assert.assertTrue(dataStore.create(user1));
                return false;
            });
            Assert.assertFalse(committed);
        }
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_user_abort");
        int[] cnt={0};
        dataStore.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId),TestUser.LABEL,(e,v)->{
            cnt[0]++;
            return true;
        });
        Assert.assertTrue(aborted[0]);
        Assert.assertEquals(cnt[0],0);
    }



    @Test(groups = { "LMDB" })
    public void testCreateIfAbsentOnCommit() {
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid).size(),10);
        };
        long ownerId = localDistributionIdGenerator.id();
        int size = 5;
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_access_committed");
                for(int i=0;i<size;i++){
                    TestAccessIndex testUser = new TestAccessIndex("user"+i);
                    testUser.ownerKey(SnowflakeKey.from(ownerId));
                    testUser.distributionId(localDistributionIdGenerator.id());
                    Assert.assertTrue(dataStore.createIfAbsent(testUser,false));
                }
                return true;
            });
        }
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_access_committed");
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),5);
    }

    @Test(groups = { "LMDB" })
    public void testCreateIfAbsentOnAbort() {
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid).size(),10);
        };
        boolean[] aborted={false};
        testMapStoreListener.abort = (tid)->{
            aborted[0]=true;
        };
        long ownerId = localDistributionIdGenerator.id();
        int size = 5;
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("test_access_abort");
                for(int i=0;i<size;i++){
                    TestAccessIndex testUser = new TestAccessIndex("user"+i);
                    testUser.ownerKey(SnowflakeKey.from(ownerId));
                    testUser.distributionId(localDistributionIdGenerator.id());
                    Assert.assertTrue(dataStore.createIfAbsent(testUser,false));
                }
                return false;
            });
        }
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_access_abort");
        int[] cnt={0};
        ds.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId),"access",(e,v)->{
            cnt[0]++;
            return true;
        });
        Assert.assertTrue(aborted[0]);
        Assert.assertEquals(cnt[0],0);
    }


    @Test(groups = { "LMDB" })
    public void testCreateWithEdge() {
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),2);
        };
        DataStore ds = lmdbDataStoreProvider.createDataStore("test_user_edge");
        long ownerId1 = localDistributionIdGenerator.id();
        List<TestUser> empty = ds.list(new TestUserQuery(ownerId1));
        Assert.assertTrue(empty.size()==0);
        List<TestUser> users = new ArrayList<>();
        for(int i=0;i<10;i++) {
            TestUser testUser = new TestUser("user"+i,ownerId1);
            Assert.assertTrue(ds.create(testUser));
            users.add(testUser);
        }
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,TestUser.LABEL)).size(),10);
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),1);
        };
        users.forEach(user->{
            Assert.assertTrue(ds.createEdge(user,"friends"));
            Assert.assertTrue(ds.createEdge(user,"games"));
        });
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),10);
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"games")).size(),10);
    }
    @Test(groups = { "LMDB" })
    public void testCreateWithEdgeOnCommit() {
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),20);
        };
        DataStore ds = lmdbDataStoreProvider.createDataStore("test_user_edge_committed");
        long ownerId1 = localDistributionIdGenerator.id();
        List<TestUser> empty = ds.list(new TestUserQuery(ownerId1));
        Assert.assertTrue(empty.size()==0);
        List<TestUser> users = new ArrayList<>();
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute((ctx)->{
                DataStore dataStore = ctx.onDataStore("test_user_edge_committed");
                for(int i=0;i<10;i++) {
                    TestUser testUser = new TestUser("user"+i,ownerId1);
                    Assert.assertTrue(dataStore.create(testUser));
                    users.add(testUser);
                }
                return true;
            });
        }
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,TestUser.LABEL)).size(),10);
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),20);
        };
        try(Transaction transaction1 = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction1.execute((ctx)->{
                DataStore dataStore = ctx.onDataStore("test_user_edge_committed");
                users.forEach(user->{
                    Assert.assertTrue(dataStore.createEdge(user,"friends"));
                    Assert.assertTrue(dataStore.createEdge(user,"games"));
                });
                return true;
            });
        }
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"friends")).size(),10);
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,"games")).size(),10);
    }

    @Test(groups = { "LMDB" })
    public void testCreateWithEdgeOnAbort() {
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),20);
        };
        testMapStoreListener.abort = (tid)->{
            //System.out.println("ABORT : "+tid);
        };
        DataStore ds = lmdbDataStoreProvider.createDataStore("test_user_edge_aborted");
        long ownerId1 = localDistributionIdGenerator.id();
        List<TestUser> empty = ds.list(new TestUserQuery(ownerId1));
        Assert.assertTrue(empty.size()==0);
        List<TestUser> users = new ArrayList<>();
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute((ctx)->{
                DataStore dataStore = ctx.onDataStore("test_user_edge_aborted");
                for(int i=0;i<10;i++) {
                    TestUser testUser = new TestUser("user"+i,ownerId1);
                    Assert.assertTrue(dataStore.create(testUser));
                    users.add(testUser);
                }
                return false;
            });
        }
        int[] cnt={0};
        ds.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId1),TestUser.LABEL,(e,v)->{
            cnt[0]++;
            return true;
        });
        Assert.assertEquals(cnt[0],0);

        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),20);
        };
        try(Transaction transaction1 = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction1.execute((ctx)->{
                DataStore dataStore = ctx.onDataStore("test_user_edge_aborted");
                users.forEach(user->{
                    Assert.assertTrue(dataStore.createEdge(user,"friends"));
                    Assert.assertTrue(dataStore.createEdge(user,"games"));
                });
                return false;
            });
        }
        cnt[0]=0;
        ds.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId1),"friends",(e,v)->{
            cnt[0]++;
            return true;
        });
        Assert.assertEquals(cnt[0],0);
        cnt[0]=0;
        ds.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId1),"games",(e,v)->{
            cnt[0]++;
            return true;
        });
        Assert.assertEquals(cnt[0],0);
    }

    @Test(groups = { "LMDB" })
    public void testTransactionLogManager() {
        DataStore foo = lmdbDataStoreProvider.createAccessIndexDataStore("test_foo_txc");
        DataStore flog = lmdbDataStoreProvider.createLogDataStore("index_a_test_foo_txc");
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid);
            logs.forEach(e->{
                e.source = "test_foo_txc";
            });
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_access_txc");

        int size = 5;
        for(int i=0;i<size;i++){
            TestAccessIndex testUser = new TestAccessIndex("user"+i);
            testUser.ownerKey(SnowflakeKey.from(ownerId));
            testUser.distributionId(localDistributionIdGenerator.id());
            Assert.assertTrue(ds.createIfAbsent(testUser,false));
            Assert.assertTrue(ds.createEdge(testUser,"links"));
        }

        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),size);
        Assert.assertEquals(flog.list(new TestAccessQuery(ownerId,"access")).size(),size);
        Assert.assertEquals(foo.list(new TestAccessQuery(ownerId,"access")).size(),size);
        Assert.assertEquals(flog.list(new TestAccessQuery(ownerId,"links")).size(),size);
        Assert.assertEquals(foo.list(new TestAccessQuery(ownerId,"links")).size(),size);
    }




}
