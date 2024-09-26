package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.lmdb.TransactionLogManager;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.DataStoreProvider;


public class DataMigrationListener implements DataStoreProvider.MigrationListener {

    private final static String data = "holee_service_presence_holee_lobby1";
    private final static String index = TransactionLogManager.DATA_PREFIX_I+data;
    private final static TarantulaLogger logger = JDKLogger.getLogger(DataMigrationListener.class);
    @Override
    public void migrate(DataStoreProvider dataStoreProvider) {

    }


    public boolean migrate(DataStore dataStore){

        if(dataStore.name().equals(data) || dataStore.name().equals(index)){
            logger.warn("Truncating data from ["+dataStore.name()+"]");
            dataStore.backup().drop(false);
        }
        return true;
    }

}
