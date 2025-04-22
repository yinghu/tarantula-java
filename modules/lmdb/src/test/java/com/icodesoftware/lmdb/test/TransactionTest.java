package com.icodesoftware.lmdb.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.Transaction;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TransactionTest extends LMDBHook{

    @Test(groups = { "native data store" })
    public void commitTest(){

        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("store_transaction_test_object");
                for(int i=0;i<2;i++) {
                    TestObject testObject = new TestObject("atype", "aname");
                    testObject.ownerKey(SnowflakeKey.from(100));
                    testObject.onEdge(true);
                    testObject.label("link");
                    dataStore.create(testObject);
                }
                DataStore access = ctx.onDataStore("store_transaction_test_access");
                for(int i=0;i<2;i++) {
                    TestObject testObject = new TestObject("atype", "aname");
                    testObject.ownerKey(SnowflakeKey.from(100));
                    testObject.onEdge(true);
                    testObject.label("provider");
                    access.create(testObject);
                }
                return true;
            });
        }

        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_transaction_test_object");
        DataStore access = lmdbDataStoreProvider.createDataStore("store_transaction_test_access");
        Assert.assertEquals(dataStore.list(new TestObjectQuery(100,"link")).size(),2);
        Assert.assertEquals(access.list(new TestObjectQuery(100,"provider")).size(),2);
    }

    @Test(groups = { "native data store" })
    public void rollbackTest(){

        try(Transaction transaction = lmdbDataStoreProvider.transaction(Distributable.DATA_SCOPE)){
            transaction.execute(ctx->{
                DataStore dataStore = ctx.onDataStore("store_transaction_test_object_abort");
                for(int i=0;i<2;i++) {
                    TestObject testObject = new TestObject("atype", "aname");
                    testObject.ownerKey(SnowflakeKey.from(100));
                    testObject.onEdge(true);
                    testObject.label("link");
                    dataStore.create(testObject);
                }
                DataStore access = ctx.onDataStore("store_transaction_test_access_abort");
                for(int i=0;i<2;i++) {
                    TestObject testObject = new TestObject("atype", "aname");
                    testObject.ownerKey(SnowflakeKey.from(100));
                    testObject.onEdge(true);
                    testObject.label("provider");
                    access.create(testObject);
                }
                return false;
            });
        }

        DataStore dataStore = lmdbDataStoreProvider.createDataStore("store_transaction_test_object_abort");
        DataStore access = lmdbDataStoreProvider.createDataStore("store_transaction_test_access_abort");
        Assert.assertEquals(dataStore.list(new TestObjectQuery(100,"link")).size(),0);
        Assert.assertEquals(access.list(new TestObjectQuery(100,"provider")).size(),0);
    }


}
