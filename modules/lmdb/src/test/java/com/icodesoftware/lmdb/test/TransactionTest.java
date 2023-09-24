package com.icodesoftware.lmdb.test;

import com.beust.ah.A;
import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;

import com.icodesoftware.lmdb.LMDBDataStoreProvider;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;

import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;



public class TransactionTest {
    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }
    LMDBDataStoreProvider lmdbDataStoreProvider;
    TestMapStoreListener testMapStoreListener;

    LocalDistributionIdGenerator localDistributionIdGenerator;
    @BeforeClass
    public void setUp() throws Exception{

        lmdbDataStoreProvider = new LMDBDataStoreProvider();
        localDistributionIdGenerator = new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        lmdbDataStoreProvider.registerDistributionIdGenerator(localDistributionIdGenerator);
        lmdbDataStoreProvider.start();
        testMapStoreListener = new TestMapStoreListener(lmdbDataStoreProvider);
        lmdbDataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,testMapStoreListener);
        lmdbDataStoreProvider.registerMapStoreListener(Distributable.INTEGRATION_SCOPE,testMapStoreListener);

    }
    @AfterTest
    public void tearDown() throws Exception{

        //lmdbDataStoreProvider.shutdown();
    }
    @Test(groups = { "LMDB" })
    public void transactionLoadTest() {
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(100));
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_f");
        Assert.assertTrue(t1.createIfAbsent(testUser,false));
        transaction.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_f");
            Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
            TestAccessIndex load = new TestAccessIndex("user");
            Assert.assertFalse(user.createIfAbsent(load,true));
            TestAccessIndex load1 = new TestAccessIndex("user");
            Assert.assertTrue(user.load(load1));
            Assert.assertEquals(load.distributionId(),id);
            Assert.assertEquals(load1.distributionId(),id);
            return true;
        });
        transaction.close();
        /**
        Transaction update = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
        update.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_f");
            Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
            TestAccessIndex load = new TestAccessIndex("user");
            Assert.assertFalse(user.createIfAbsent(load,true));
            Assert.assertTrue(load.distributionId()==id);
            load.distributionId(100);
            Assert.assertTrue(user.update(load));
            return true;
        });
        update.close();
        **/
        //DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_f");
        //TestAccessIndex load = new TestAccessIndex("user");
        //Assert.assertTrue(t1.load(load));
        //Assert.assertEquals(load.distributionId(),100);
        //Assert.assertEquals(load.revision(),Long.MIN_VALUE+1);
    }

    @Test(groups = { "LMDB" })
    public void transactionUpdateTest() {
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(100));
        transaction.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_d");
            Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
            Assert.assertTrue(user.createIfAbsent(testUser,false));
            return true;
        });
        transaction.close();
        Transaction update = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
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
        update.close();

        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_d");
        TestAccessIndex load = new TestAccessIndex("user");
        Assert.assertTrue(t1.load(load));
        Assert.assertEquals(load.distributionId(),100);
        Assert.assertEquals(load.revision(),Long.MIN_VALUE+1);
    }
    @Test(groups = { "LMDB" })
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
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestAccessIndex testUser = new TestAccessIndex("user");
        testUser.distributionId(id);
        testUser.onEdge(true);
        testUser.label(TestUser.LABEL);
        testUser.ownerKey(new SnowflakeKey(100));
        transaction.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_a");
            Assert.assertTrue(user.scope()==Distributable.INTEGRATION_SCOPE);
            Assert.assertTrue(user.createIfAbsent(testUser,false));
            DataStore account = ctx.onDataStore("test_account_a");
            Assert.assertTrue(account.scope()==Distributable.INTEGRATION_SCOPE);
            Assert.assertTrue(account.createIfAbsent(testUser,false));
            return true;
        });
        transaction.close();
        DataStore t1 = lmdbDataStoreProvider.createAccessIndexDataStore("test_user_a");
        DataStore t2 = lmdbDataStoreProvider.createAccessIndexDataStore("test_account_a");
        Assert.assertTrue(t1.load(testUser));
        Assert.assertEquals(t1.list(new TestAccessQuery(100)).size(),1);
        Assert.assertTrue(t2.load(testUser));
        Assert.assertEquals(t2.list(new TestAccessQuery(100)).size(),1);
    }
    @Test(groups = { "LMDB" })
    public void transactionCreateTest() {
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        long id = localDistributionIdGenerator.id();
        Assert.assertTrue(id>0);
        TestUser testUser = new TestUser("user",id);
        transaction.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_p");
            Assert.assertTrue(user.create(testUser));
            DataStore account = ctx.onDataStore("test_account_p");
            Assert.assertTrue(account.create(testUser));
            return true;
        });
        transaction.close();

        Transaction t2 = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        t2.execute(ctx->{
            DataStore user = ctx.onDataStore("test_user_p");
            Assert.assertTrue(user.create(testUser));
            DataStore account = ctx.onDataStore("test_account_p");
            Assert.assertTrue(account.create(testUser));
            return true;
        });
        t2.close();


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
        Assert.assertEquals(ct[0],8);
    }

}
