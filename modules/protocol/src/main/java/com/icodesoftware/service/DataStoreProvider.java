package com.icodesoftware.service;


import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;
import com.icodesoftware.Transaction;

import java.util.List;
import java.util.Map;


public interface DataStoreProvider extends ServiceProvider {

    String NAME = "tarantula";


    void configure(Map<String,Object> properties);

    void registerDistributionIdGenerator(DistributionIdGenerator distributionIdGenerator);
    void registerMapStoreListener(int scope, MapStoreListener mapStoreListener);

    void backup(int scope);

    //create none-partitioned integration scope data store
    DataStore createAccessIndexDataStore(String name);

    //create none-partitioned local scope data store
    DataStore createKeyIndexDataStore(String name);

    DataStore createDataStore(String name);
    //create partitioned data scope data store
    DataStore createLocalDataStore(String name);

    DataStore createLogDataStore(String name);

    List<String> list();

    List<String> list(int scope);

    DataStore lookup(String name);

    Transaction transaction(int scope);

    interface OnStart{
        void on(DataStoreProvider dataStoreProvider);
    }

    interface DistributionIdGenerator{
        long id();
        void assign(Recoverable.DataBuffer dataBuffer);
    }

}
