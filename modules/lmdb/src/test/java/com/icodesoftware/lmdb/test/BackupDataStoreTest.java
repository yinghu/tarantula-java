package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;
import com.icodesoftware.lmdb.TransactionLog;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class BackupDataStoreTest extends LMDBHook{

    @Test(groups = { "LMDBBackup" })
    public void testSummary(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_backup_user");
        TestUser user = new TestUser("test001",ownerId);
        Assert.assertTrue(dataStore.create(user));
        TestUser user1 = new TestUser("test002",ownerId);
        Assert.assertTrue(dataStore.create(user1));
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);
        TestSummary summary = new TestSummary();
        dataStore.backup().view(summary);
        Assert.assertEquals(summary.count(),2);
        Assert.assertEquals(summary.edgeList().size(),1);

        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore1 = ctx.onDataStore("test_backup_user");
                dataStore1.backup().view(summary);
                Assert.assertEquals(summary.count(),2);
                Assert.assertEquals(summary.edgeList().size(),0);
                return true;
            });
        }
    }

    @Test(groups = { "LMDBBackup" })
    public void testGet(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_backup_user_get");
        TestUser user = new TestUser("test001",ownerId);
        Assert.assertTrue(dataStore.create(user));
        TestUser user1 = new TestUser("test002",ownerId);
        Assert.assertTrue(dataStore.create(user1));
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);

        int[] ct ={0};
        Assert.assertTrue(dataStore.backup().get(user.key(),(k,v)->{
            TestUser getUser = new TestUser();
            getUser.readKey(k);
            Recoverable.DataHeader h = v.readHeader();
            getUser.read(v);
            getUser.revision(h.revision());
            Assert.assertEquals(user.distributionId(),getUser.distributionId());
            ct[0] = 100;
            return true;
        }));
        Assert.assertEquals(ct[0],100);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore1 = ctx.onDataStore("test_backup_user_get");
                dataStore1.backup().get(user1.key(),(k,v)->{
                    TestUser getUser = new TestUser();
                    getUser.readKey(k);
                    Recoverable.DataHeader h = v.readHeader();
                    getUser.read(v);
                    getUser.revision(h.revision());
                    Assert.assertEquals(user1.distributionId(),getUser.distributionId());
                    ct[0] = 200;
                    return true;
                });
                return true;
            });
        }
        Assert.assertEquals(ct[0],200);
    }

    @Test(groups = { "LMDBBackup" })
    public void testForEach(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_backup_user_for");
        TestUser user = new TestUser("test001",ownerId);
        Assert.assertTrue(dataStore.create(user));
        TestUser user1 = new TestUser("test002",ownerId);
        Assert.assertTrue(dataStore.create(user1));
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);

        int[] ct ={0};
        dataStore.backup().forEach((k,v)->{
            TestUser getUser = new TestUser();
            Assert.assertTrue(getUser.readKey(k));
            Recoverable.DataHeader h = v.readHeader();
            Assert.assertTrue(getUser.read(v));
            getUser.revision(h.revision());
            Assert.assertTrue(getUser.distributionId()>0);
            ct[0]++;
            return true;
        });
        Assert.assertEquals(ct[0],2);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore1 = ctx.onDataStore("test_backup_user_for");
                dataStore1.backup().forEach((k,v)->{
                    TestUser getUser = new TestUser();
                    Assert.assertTrue(getUser.readKey(k));
                    Recoverable.DataHeader h = v.readHeader();
                    Assert.assertTrue(getUser.read(v));
                    getUser.revision(h.revision());
                    Assert.assertTrue(getUser.distributionId()>0);
                    ct[0]++;
                    return true;
                });
                return true;
            });
        }
        Assert.assertEquals(ct[0],4);
    }

    @Test(groups = { "LMDBBackup" })
    public void testForEachEdge(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_backup_user_for_edge");
        TestUser user = new TestUser("test001",ownerId);
        Assert.assertTrue(dataStore.create(user));
        TestUser user1 = new TestUser("test002",ownerId);
        Assert.assertTrue(dataStore.create(user1));
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);

        int[] ct ={0};
        ArrayList<TestUser> pending = new ArrayList<>();
        dataStore.backup().forEachEdgeKey(SnowflakeKey.from(ownerId),"users",(k,v)->{
            TestUser getUser = new TestUser();
            Assert.assertTrue(getUser.readKey(v));
            ct[0]++;
            pending.add(getUser);
            return true;
        });
        Assert.assertEquals(ct[0],2);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore1 = ctx.onDataStore("test_backup_user_for_edge");
                dataStore1.backup().forEachEdgeKey(SnowflakeKey.from(ownerId),"users",(k,v)->{
                    TestUser getUser = new TestUser();
                    Assert.assertTrue(getUser.readKey(v));
                    pending.add(getUser);
                    ct[0]++;
                    return true;
                });
                return true;
            });
        }
        Assert.assertEquals(ct[0],4);
        Assert.assertEquals(pending.size(),4);
        pending.forEach((u)->{
            Assert.assertTrue(dataStore.load(u));
        });
    }

    @Test(groups = { "LMDBBackup" })
    public void testForEachEdgeValue(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<TransactionLog> logs = testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid);
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_backup_user_for_edge_value");
        TestUser user = new TestUser("test001",ownerId);
        Assert.assertTrue(dataStore.create(user));
        TestUser user1 = new TestUser("test002",ownerId);
        Assert.assertTrue(dataStore.create(user1));
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);

        int[] ct ={0};
        ArrayList<TestUser> pending = new ArrayList<>();
        dataStore.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId),"users",(k,v)->{
            TestUser getUser = new TestUser();
            Assert.assertTrue(getUser.readKey(k));
            Recoverable.DataHeader h = v.readHeader();
            Assert.assertTrue(getUser.read(v));
            getUser.revision(h.revision());
            Assert.assertTrue(getUser.distributionId()>0);
            ct[0]++;
            pending.add(getUser);
            return true;
        });
        Assert.assertEquals(ct[0],2);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore1 = ctx.onDataStore("test_backup_user_for_edge_value");
                dataStore1.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId),"users",(k,v)->{
                    TestUser getUser = new TestUser();
                    Assert.assertTrue(getUser.readKey(k));
                    Recoverable.DataHeader h = v.readHeader();
                    Assert.assertTrue(getUser.read(v));
                    getUser.revision(h.revision());
                    Assert.assertTrue(getUser.distributionId()>0);
                    pending.add(getUser);
                    ct[0]++;
                    return true;
                });
                return true;
            });
        }
        //Assert.assertEquals(ct[0],4);
        //Assert.assertEquals(pending.size(),4);
        pending.forEach((u)->{
            //Assert.assertTrue(dataStore.load(u));
        });
    }

}
