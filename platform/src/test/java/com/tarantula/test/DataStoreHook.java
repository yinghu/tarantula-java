package com.tarantula.test;

import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.service.persistence.TransactionLogManager;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;

public class DataStoreHook {

    protected DataStoreProvider dataStoreProvider;
    protected ServiceContext serviceContext;

    protected String name;
    protected TransactionLogManager transactionLogManager;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
        transactionLogManager = DataStoreTestEvn.mapStoreListener.transactionLogManager;
    }
    @AfterTest
    public void tearDown() throws Exception{
        try{
            DataStoreTestEvn.dataStoreProvider.shutdown();
        }catch (Exception ex){
            //System.out.println(name);
            ex.printStackTrace();
        }
    }

}
