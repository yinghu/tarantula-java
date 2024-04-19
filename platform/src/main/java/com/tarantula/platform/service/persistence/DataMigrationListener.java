package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;


public class DataMigrationListener implements DataStoreProvider.MigrationListener {


    @Override
    public void migrate(DataStoreProvider dataStoreProvider) {

    }


    public boolean migrate(DataStore dataStore){
        if(!dataStore.name().equals("holee_service_presence_holee_lobby1")) return true;
        dataStore.backup().drop(false);
        return false;
    }

}
