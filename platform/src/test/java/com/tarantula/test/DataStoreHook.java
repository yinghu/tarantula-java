package com.tarantula.test;

import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;

public class DataStoreHook {

    protected DataStoreProvider dataStoreProvider;
    protected ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }
    @AfterTest
    public void tearDown() throws Exception{
        dataStoreProvider.shutdown();
    }

}
