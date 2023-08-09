package com.tarantula.test;

import com.icodesoftware.Distributable;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.DataStoreConfigurationJsonParser;
import com.tarantula.platform.service.persistence.MapStoreListener;

public class DataStoreTestEvn {

    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }

    static DataStoreProvider dataStoreProvider;
    static ServiceContext serviceContext;

    static MapStoreListener mapStoreListener = new TestMapStoreListener();
    public static void setUp() {
        serviceContext = new TestServiceContext();
        DataStoreConfigurationJsonParser parser = new DataStoreConfigurationJsonParser("test-tarantula-platform-data-store-config.json",serviceContext, 3,dataStoreProvider->{
            try{
                DataStoreTestEvn.dataStoreProvider = dataStoreProvider;
                DataStoreTestEvn.dataStoreProvider.start();
                DataStoreTestEvn.dataStoreProvider.setup(serviceContext);
                DataStoreTestEvn.dataStoreProvider.registerMapStoreListener(Distributable.INDEX_SCOPE,mapStoreListener);
                DataStoreTestEvn.dataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,mapStoreListener);
                DataStoreTestEvn.dataStoreProvider.registerMapStoreListener(Distributable.INTEGRATION_SCOPE,mapStoreListener);
                DataStoreTestEvn.dataStoreProvider.waitForData();

            }catch (Exception exx){
                exx.printStackTrace();
                throw new RuntimeException(exx);
            }
        });
        try {
            parser.start();
        }catch (Exception ex){
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
