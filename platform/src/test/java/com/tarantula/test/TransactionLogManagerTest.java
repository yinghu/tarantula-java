package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import org.testng.Assert;
import org.testng.annotations.Test;


public class TransactionLogManagerTest extends DataStoreHook{


    @Test(groups = { "TransactionLogManager" })
    public void transactionLogManagerTest(){
        DataStore dataStore = serviceContext.dataStore(Distributable.DATA_SCOPE,"test");
        
        Assert.assertEquals(1,1);
    }

}
