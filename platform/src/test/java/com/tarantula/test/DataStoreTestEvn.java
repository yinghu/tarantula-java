package com.tarantula.test;

import com.icodesoftware.Distributable;
import com.icodesoftware.lmdb.LocalDistributionIdGenerator;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.service.persistence.DataStoreConfigurationJsonParser;
import com.icodesoftware.service.MapStoreListener;

public class DataStoreTestEvn {

    static {
        System.setProperty("java.util.logging.manager","com.icodesoftware.logging.TarantulaLogManager");
    }

    static DataStoreProvider dataStoreProvider;
    static ServiceContext serviceContext;

    static DataStoreProvider.DistributionIdGenerator distributionIdGenerator;
    static TestMapStoreListener mapStoreListener = new TestMapStoreListener();

    static boolean started = false;
    public static void setUp() {
        if(started) return;
        started = true;
        distributionIdGenerator = new LocalDistributionIdGenerator(1, TimeUtil.epochMillisecondsFromMidnight(2020,1,1));
        serviceContext = new TestServiceContext(distributionIdGenerator);
        DataStoreConfigurationJsonParser parser = new DataStoreConfigurationJsonParser("test-tarantula-platform-data-store-config.json",serviceContext, 3,dataStoreProvider->{
            try{
                DataStoreTestEvn.dataStoreProvider = dataStoreProvider;
                DataStoreTestEvn.dataStoreProvider.registerDistributionIdGenerator(distributionIdGenerator);
                DataStoreTestEvn.dataStoreProvider.start();
                DataStoreTestEvn.dataStoreProvider.setup(serviceContext);
                //DataStoreTestEvn.dataStoreProvider.registerMapStoreListener(Distributable.INDEX_SCOPE,mapStoreListener);
                DataStoreTestEvn.dataStoreProvider.registerMapStoreListener(Distributable.DATA_SCOPE,mapStoreListener);
                DataStoreTestEvn.dataStoreProvider.registerMapStoreListener(Distributable.INTEGRATION_SCOPE,mapStoreListener);
                DataStoreTestEvn.dataStoreProvider.waitForData();
                ((TestServiceContext)serviceContext).dataStoreProvider = dataStoreProvider;
                //mapStoreListener.dataStoreProvider = dataStoreProvider;
                mapStoreListener.serviceContext = serviceContext;
                mapStoreListener.start();

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
