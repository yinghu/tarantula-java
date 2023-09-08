package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.DataStoreProvider;
import com.tarantula.platform.service.cluster.ClusterFailureEvent;
import com.tarantula.platform.store.Transaction;
import com.tarantula.platform.store.TransactionEventLogger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TransactionServiceEventLoggerTest extends DataStoreHook{


    //@Test(groups = { "eventLoggerTest" })
    public void loggerTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_event_log");
        TransactionEventLogger transactionEventLogger = new TransactionEventLogger(dataStore);
        Transaction transaction = new Transaction("","","");
        transaction.index("transaction0011");
        Exception exception = null;
        try{
            transactionEventLogger.log(transaction);
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
        try{
            transactionEventLogger.log(transaction);
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertTrue(dataStore.load(transaction));
        exception = null;
        try{
            transactionEventLogger.log(new ClusterFailureEvent("","",new Exception("")));
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNotNull(exception);
    }



}
