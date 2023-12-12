package com.icodesoftware.lmdb.test;

import com.icodesoftware.*;
import com.icodesoftware.lmdb.*;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.util.BinaryKey;
import com.icodesoftware.util.NaturalKey;

import com.icodesoftware.util.SnowflakeKey;
import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.swing.plaf.basic.BasicIconFactory;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class LMDBDataStoreTest {
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
    public void testCreate(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),2);
        };
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_user");
        TestUser user = new TestUser("test001",ownerId);
        Assert.assertTrue(dataStore.create(user));
        TestUser user1 = new TestUser("test002",ownerId);
        Assert.assertTrue(dataStore.create(user1));
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);
        try(Recoverable.DataBufferPair cache = lmdbDataStoreProvider.dataBufferPair()){
            cache.key().writeDouble(100);
            cache.value().writeDouble(100);
        }catch (Exception ex){

        }
    }
    @Test(groups = { "LMDB" })
    public void testCommitOnTransaction(){
        long ownerId = localDistributionIdGenerator.id();
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),4);
        };
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        boolean committed = transaction.execute(ctx->{
            DataStore dataStore = ctx.onDataStore("test_user_committed");
            TestUser user = new TestUser("test001",ownerId);
            Assert.assertTrue(dataStore.create(user));
            TestUser user1 = new TestUser("test002",ownerId);
            Assert.assertTrue(dataStore.create(user1));
            return true;
        });
        Assert.assertTrue(committed);
        DataStore dataStore = lmdbDataStoreProvider.createDataStore("test_user_committed");
        Assert.assertEquals(dataStore.list(new TestUserQuery(ownerId)).size(),2);
    }
    @Test(groups = { "LMDB" })
    public void testAbortOnTransaction(){
        long ownerId = localDistributionIdGenerator.id();
        boolean[] aborted={false};
        testMapStoreListener.abort = (tid)->{
            aborted[0]=true;
        };
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        boolean committed = transaction.execute(ctx->{
            DataStore dataStore = ctx.onDataStore("test_user_abort");
            TestUser user = new TestUser("test001",ownerId);
            Assert.assertTrue(dataStore.create(user));
            TestUser user1 = new TestUser("test002",ownerId);
            Assert.assertTrue(dataStore.create(user1));
            return false;
        });
        Assert.assertFalse(committed);
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
    public void testCreateIfAbsent() {
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid).size(),2);
        };
        DataStore ds = lmdbDataStoreProvider.createAccessIndexDataStore("test_access");
        long ownerId = localDistributionIdGenerator.id();
        int size = 5;
        for(int i=0;i<size;i++){
            TestAccessIndex testUser = new TestAccessIndex("user"+i);
            testUser.ownerKey(SnowflakeKey.from(ownerId));
            testUser.distributionId(localDistributionIdGenerator.id());
            Assert.assertTrue(ds.createIfAbsent(testUser,false));
        }
        Assert.assertEquals(ds.list(new TestAccessQuery(ownerId,"access")).size(),5);
    }

    @Test(groups = { "LMDB" })
    public void testCreateIfAbsentOnCommit() {
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.INTEGRATION_SCOPE,tid).size(),10);
        };
        long ownerId = localDistributionIdGenerator.id();
        int size = 5;
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
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
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.INTEGRATION_SCOPE);
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
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        transaction.execute((ctx)->{
            DataStore dataStore = ctx.onDataStore("test_user_edge_committed");
            for(int i=0;i<10;i++) {
                TestUser testUser = new TestUser("user"+i,ownerId1);
                Assert.assertTrue(dataStore.create(testUser));
                users.add(testUser);
            }
            return true;
        });
        Assert.assertEquals(ds.list(new TestUserQuery(ownerId1,TestUser.LABEL)).size(),10);
        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),20);
        };
        Transaction transaction1 = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        transaction1.execute((ctx)->{
            DataStore dataStore = ctx.onDataStore("test_user_edge_committed");
            users.forEach(user->{
                Assert.assertTrue(dataStore.createEdge(user,"friends"));
                Assert.assertTrue(dataStore.createEdge(user,"games"));
            });
            return true;
        });
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
        Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        transaction.execute((ctx)->{
            DataStore dataStore = ctx.onDataStore("test_user_edge_aborted");
            for(int i=0;i<10;i++) {
                TestUser testUser = new TestUser("user"+i,ownerId1);
                Assert.assertTrue(dataStore.create(testUser));
                users.add(testUser);
            }
            return false;
        });
        int[] cnt={0};
        ds.backup().forEachEdgeKeyValue(SnowflakeKey.from(ownerId1),TestUser.LABEL,(e,v)->{
            cnt[0]++;
            return true;
        });
        Assert.assertEquals(cnt[0],0);

        testMapStoreListener.verifier = (tid)->{
            Assert.assertEquals(testMapStoreListener.transactionLogManager.committed(Distributable.DATA_SCOPE,tid).size(),20);
        };
        Transaction transaction1 = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE);
        transaction1.execute((ctx)->{
            DataStore dataStore = ctx.onDataStore("test_user_edge_aborted");
            users.forEach(user->{
                Assert.assertTrue(dataStore.createEdge(user,"friends"));
                Assert.assertTrue(dataStore.createEdge(user,"games"));
            });
            return false;
        });
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
