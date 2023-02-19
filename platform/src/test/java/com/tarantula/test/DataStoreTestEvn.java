package com.tarantula.test;

import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.DataStoreConfigurationJsonParser;

public class DataStoreTestEvn {

    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }

    static DataStoreProvider dataStoreProvider;
    static ServiceContext serviceContext;

    public static void setUp() {
        serviceContext = new TestServiceContext();
        DataStoreConfigurationJsonParser parser = new DataStoreConfigurationJsonParser("test-tarantula-platform-data-store-config.json",serviceContext, dataStoreProvider->{
            try{
                DataStoreTestEvn.dataStoreProvider = dataStoreProvider;
                DataStoreTestEvn.dataStoreProvider.start();
                DataStoreTestEvn.dataStoreProvider.setup(serviceContext);
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
