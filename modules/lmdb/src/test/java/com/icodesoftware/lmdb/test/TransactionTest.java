package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;

import com.icodesoftware.lmdb.TransactionLog;

import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;

import org.testng.annotations.Test;

import java.util.List;


public class TransactionTest extends LMDBHook{


    @Test(groups = { "LMDB" })
    public void transactionBackupWriteCallsTest() {
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_m");
        for(int i=0;i<2;i++) {
            TestAccessIndex testUser = new TestAccessIndex("user"+i);
            testUser.distributionId(id);
            testUser.onEdge(true);
            testUser.label(TestUser.LABEL);
            testUser.ownerKey(new SnowflakeKey(100));
            Assert.assertTrue(t1.createIfAbsent(testUser, false));
            Assert.assertTrue(t1.createEdge(testUser, "friend"));
            Assert.assertTrue(t1.createEdge(testUser, "slots"));
        }
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),2);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),2);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"slots")).size(),2);
        int[] ctn ={0};
        t1.backup().forEachEdgeKeyValue(SnowflakeKey.from(100),"friend",(e,v)->{
            ctn[0]++;
            return true;
        });
        Assert.assertEquals(ctn[0],2);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_m");
                user.backup().set((k,v)->{
                    k.writeLong(333);
                    v.writeLong(666);
                    return true;
                });
                user.backup().get(new SnowflakeKey(333),(k,v)->{
                    return true;
                });
                user.backup().unset((k,v)->{
                    k.writeLong(333);
                    return true;
                });
                int[] ct={0};
                Assert.assertFalse(user.backup().get(new SnowflakeKey(333),(k,v)->{
                    ct[0]++;
                    return true;
                }));
                Assert.assertEquals(ct[0],0);
                user.backup().setEdge("pws",(k,v)->{
                    k.writeLong(999);
                    v.writeLong(111);
                    return true;
                });
                user.backup().setEdge("pws",(k,v)->{
                    k.writeLong(999);
                    v.writeLong(222);
                    return true;
                });
                ct[0]=0;
                user.backup().forEachEdgeKey(new SnowflakeKey(999),"pws",(k,v)->{
                    //System.out.println(v.readLong());
                    ct[0]++;
                    return true;
                });
                Assert.assertEquals(ct[0],2);
                boolean suc = user.backup().unsetEdge("pws",(k,v)->{
                    k.writeLong(999);
                    v.writeLong(111);
                    return true;
                },false);
                Assert.assertTrue(suc);
                ct[0]=0;
                user.backup().forEachEdgeKey(new SnowflakeKey(999),"pws",(k,v)->{
                    //System.out.println(v.readLong());
                    ct[0]++;
                    return true;
                });
                Assert.assertEquals(ct[0],1);
                suc = user.backup().unsetEdge("pws",(k,v)->{
                    k.writeLong(999);
                    return true;
                },true);
                Assert.assertTrue(suc);
                ct[0]=0;
                user.backup().forEachEdgeKey(new SnowflakeKey(999),"pws",(k,v)->{
                    //System.out.println(v.readLong());
                    ct[0]++;
                    return true;
                });
                Assert.assertEquals(ct[0],0);
                return true;
            });
        }
    }

    @Test(groups = { "LMDB" })
    public void transactionBackupReadCallsTest() {

        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_k");
        for(int i=0;i<2;i++) {
            TestAccessIndex testUser = new TestAccessIndex("user"+i);
            testUser.distributionId(id);
            testUser.onEdge(true);
            testUser.label(TestUser.LABEL);
            testUser.ownerKey(new SnowflakeKey(100));
            Assert.assertTrue(t1.createIfAbsent(testUser, false));
            Assert.assertTrue(t1.createEdge(testUser, "friend"));
            Assert.assertTrue(t1.createEdge(testUser, "slots"));
        }
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),2);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),2);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"slots")).size(),2);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_k");
                TestAccessIndex testUser = new TestAccessIndex("user0");
                user.backup().get(testUser.key(),(k,v)->{
                    Recoverable.DataHeader h = v.readHeader();
                    Assert.assertEquals(h.classId(),100);
                    return true;
                });
                int[] ct ={0};
                user.backup().forEach((k,v)->{
                    Recoverable.DataHeader h = v.readHeader();
                    Assert.assertEquals(h.classId(),100);
                    ct[0]++;
                    return true;
                });
                Assert.assertEquals(ct[0],2);
                ct[0]=0;
                user.backup().forEachEdgeKeyValue(new SnowflakeKey(100),"friend",(e,v)->{
                    ct[0]++;
                    return true;
                });
                Assert.assertEquals(ct[0],2);
                return true;
            });
        }

    }

    @Test(groups = { "LMDB" })
    public void transactionListTest() {
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_j");
        for(int i=0;i<2;i++) {
            TestAccessIndex testUser = new TestAccessIndex("user"+i);
            testUser.distributionId(id);
            testUser.onEdge(true);
            testUser.label(TestUser.LABEL);
            testUser.ownerKey(new SnowflakeKey(100));
            Assert.assertTrue(t1.createIfAbsent(testUser, false));
            Assert.assertTrue(t1.createEdge(testUser, "friend"));
            Assert.assertTrue(t1.createEdge(testUser, "slots"));
        }
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),2);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),2);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"slots")).size(),2);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_j");
                Assert.assertEquals(user.list(new TestAccessQuery(100)).size(),2);
                Assert.assertEquals(user.list(new TestAccessQuery(100,"friend")).size(),2);
                Assert.assertEquals(user.list(new TestAccessQuery(100,"slots")).size(),2);
                return true;
            });
        }
    }
    @Test(groups = { "LMDB" })
    public void transactionDeleteEdgesTest() {

        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_i");
        for(int i=0;i<2;i++) {
            TestAccessIndex testUser = new TestAccessIndex("user"+i);
            testUser.distributionId(id);
            testUser.onEdge(true);
            testUser.label(TestUser.LABEL);
            testUser.ownerKey(new SnowflakeKey(100));
            Assert.assertTrue(t1.createIfAbsent(testUser, false));
            Assert.assertTrue(t1.createEdge(testUser, "friend"));
            Assert.assertTrue(t1.createEdge(testUser, "slots"));
        }
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),2);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),2);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"slots")).size(),2);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_i");
                Assert.assertTrue(user.deleteEdge(new SnowflakeKey(100),"friend"));
                Assert.assertTrue(user.deleteEdge(new SnowflakeKey(100),"users"));
                Assert.assertTrue(user.deleteEdge(new SnowflakeKey(100),"slots"));

                return true;
            });
        }
        Assert.assertTrue(t1.load(new TestAccessIndex("user0")));
        Assert.assertTrue(t1.load(new TestAccessIndex("user1")));
        //Assert.assertTrue(t1.load(new TestAccessIndex("user0")));
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),0);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),0);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),0);
    }
    @Test(groups = { "LMDB" })
    public void transactionDeleteEdgeTest() {
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(100));
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_h");
        Assert.assertTrue(t1.createIfAbsent(testUser,false));
        Assert.assertTrue(t1.createEdge(testUser,"friend"));
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),1);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),1);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_h");
                Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
                TestAccessIndex loaded = new TestAccessIndex("user");
                Assert.assertTrue(user.load(loaded));
                Assert.assertTrue(user.deleteEdge(testUser.ownerKey(),testUser.key(),"friend"));
                return true;
            });
        }
        TestAccessIndex deleted1 = new TestAccessIndex("user");
        Assert.assertTrue(t1.load(deleted1));
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),1);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),0);
    }

    @Test(groups = { "LMDB" })
    public void transactionDeleteTest() {

        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(100));
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_g");
        Assert.assertTrue(t1.createIfAbsent(testUser,false));
        Assert.assertTrue(t1.createEdge(testUser,"friend"));
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),1);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_g");
                Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
                TestAccessIndex loaded = new TestAccessIndex("user");
                Assert.assertTrue(user.load(loaded));
                Assert.assertTrue(user.delete(loaded));
                return true;
            });
        }
        TestAccessIndex deleted1 = new TestAccessIndex("user");
        Assert.assertFalse(t1.load(deleted1));
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),0);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),0);
    }
    @Test(groups = { "LMDB" })
    public void transactionLoadTest() {

        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(200));
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_f");
        Assert.assertTrue(t1.createIfAbsent(testUser,false));
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_f");
                Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
                TestAccessIndex load = new TestAccessIndex("user");
                Assert.assertFalse(user.createIfAbsent(load,true));
                TestAccessIndex load1 = new TestAccessIndex("user");
                Assert.assertTrue(user.load(load1));
                Assert.assertEquals(load.distributionId(),id);
                Assert.assertEquals(load1.distributionId(),id);
                return false;//abort transaction
            });
        }
        TestAccessIndex reload = new TestAccessIndex("user");
        Assert.assertTrue(t1.load(reload));
    }

    @Test(groups = { "LMDB" })
    public void transactionUpdateTest() {
        ;
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(100));
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_d");
                Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
                Assert.assertTrue(user.createIfAbsent(testUser,false));
                return true;
            });
        }
        try(Transaction update = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            update.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_d");
                Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
                TestAccessIndex load = new TestAccessIndex("user");
                Assert.assertFalse(user.createIfAbsent(load,true));
                Assert.assertTrue(load.distributionId()==id);
                load.distributionId(100);
                Assert.assertTrue(user.update(load));
                return true;
            });
        }
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_d");
        TestAccessIndex load = new TestAccessIndex("user");
        Assert.assertTrue(t1.load(load));
        Assert.assertEquals(load.distributionId(),100);
        Assert.assertEquals(load.revision(),Long.MIN_VALUE+1);
    }
    //@Test(groups = { "LMDB" })
    public void transactionCreateEdgeTest() {
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(100));
        transaction.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_c");
            Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
            Assert.assertTrue(user.createIfAbsent(testUser,false));
            Assert.assertTrue(user.createEdge(testUser,"friend"));
            Assert.assertTrue(user.createEdge(testUser,"slots"));
            return true;
        });
        transaction.close();
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_c");
        Assert.assertTrue(t1.load(testUser));
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),1);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"friend")).size(),1);
        Assert.assertEquals(t1.list(new TestAccessQuery(100,"slots")).size(),1);
    }
    @Test(groups = { "LMDB" })
    public void transactionCreateIfAbsentTest() {

        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(100));
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore user = ctx.onDataStore("test_user_a");
                Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
                Assert.assertTrue(user.createIfAbsent(testUser,false));
                DataStore account = ctx.onDataStore("test_account_a");
                Assert.assertTrue(account.scope()==Distributable.INTEGRATION_SCOPE);
                Assert.assertTrue(account.createIfAbsent(testUser,false));
                return true;
            });
        }
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_a");
        DataStore t2 = lmdbDataStoreProvider.createAccessIndexDataStore("test_account_a");
        Assert.assertTrue(t1.load(testUser));
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),1);
        Assert.assertTrue(t2.load(testUser));
        Assert.assertEquals(t2.list(new TestAccessQuery(100)).size(),1);
    }
    @Test(groups = { "LMDB" })
    public void transactionCreateTest() {
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestUser testUser = new TestUser("user",id);
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
        transaction.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_p");
            Assert.assertTrue(user.create(testUser));
            DataStore account = ctx.onDataStore("test_account_p");
            Assert.assertTrue(account.create(testUser));
            return true;
        });
        }

        try(Transaction t2 = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
        t2.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_p");
            Assert.assertTrue(user.create(testUser));
            DataStore account = ctx.onDataStore("test_account_p");
            Assert.assertTrue(account.create(testUser));
            return true;
        });
        }

        DataStore p1 = lmdbDataStoreProvider.createDataStore("test_user_p");
        int[] ct={0};
        p1.backup().forEach((k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            Assert.assertEquals(h.classId(),testUser.getClassId());
            ct[0]++;
            return true;
        });
        p1.list(new TestUserQuery(id)).forEach(a->{
            ct[0]++;
            Assert.assertNotNull(a.login());
        });
        DataStore p2 = lmdbDataStoreProvider.createDataStore("test_account_p");
        p2.backup().forEach((k,v)->{
            Recoverable.DataHeader h = v.readHeader();
            Assert.assertEquals(h.classId(),testUser.getClassId());
            ct[0]++;
            return true;
        });
        p2.list(new TestUserQuery(id)).forEach(a->{
            ct[0]++;
            Assert.assertNotNull(a.login());
        });
        Assert.assertEquals(ct[0],4);
    }

    @Test(groups = { "LMDB" })
    public void testTransactionLogManager() {
        //DataStore foo = lmdbDataStoreProvider.createAccessIndexDataStore("test_bar_txc");
        //DataStore flog = lmdbDataStoreProvider.createLogDataStore("log_a_test_bar_txc");
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            List<Transaction.Log> logs = testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid);
            logs.forEach(e->{
                e.source("test_bar_txc");
            });
            testMapStoreListener.transactionLogManager.onTransaction(logs);
        };
        //DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_access_txc");

        int size = 5;
        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
            transaction.execute(ctx->{
                DataStore dts = ctx.onDataStore("test_access_txc");
                int ct = 0;
                for(int i=0;i<size;i++){
                    TestAccessIndex testUser = new TestAccessIndex("userbx"+i);
                    testUser.ownerKey(SnowflakeKey.from(ownerId));
                    testUser.distributionId(localDistributionIdGenerator.id());
                    Assert.assertTrue(dts.createIfAbsent(testUser,false));
                    ct++;
                }
                return ct==size;
            });
        }
        try(Transaction transaction1 = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE)){
        transaction1.execute(ctx->{
            DataStore bar = ctx.onDataStore("test_bar_txc");
            Assert.assertEquals(bar.list(new TestAccessQuery(ownerId,"access")).size(),size);
            return true;
        });
        }
        //Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),size);
        //Assert.assertEquals(flog.list(new TestAccessQuery(ownerId,"access")).size(),size);
        //Assert.assertEquals(foo.list(new TestAccessQuery(ownerId,"access")).size(),size);
    }

}
