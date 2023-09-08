package com.icodesoftware.service;


import com.icodesoftware.DataStore;
import com.icodesoftware.Recoverable;

import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;


public interface DataStoreProvider extends ServiceProvider {

    String NAME = "tarantula";


    void configure(Map<String,Object> properties);

    void registerDistributionIdGenerator(DistributionIdGenerator distributionIdGenerator);
    void registerMapStoreListener(int scope, MapStoreListener mapStoreListener);

    MapStoreListener mapStoreListener(int scope);

    //create none-partitioned integration scope data store
    DataStore createAccessIndexDataStore(String name);

    //create none-partitioned local scope data store
    DataStore createKeyIndexDataStore(String name);

    DataStore createDataStore(String name);
    //create partitioned data scope data store
    default DataStore create(String name,int partition){return  null;}

    List<String> list();
    DataStore lookup(String name);

    //incremental back up
    void backup(int scope);

    //full back up with callback recover
    void backup(int scope,OnBackup backup);

    //recover from incremental back up
    void recover(int scope,OnBackup backup);

    //long nextId(String name);
    interface OnBackup{
        void on(String fName,int fSize,ReadableByteChannel in);
    }

    interface OnStart{
        void on(DataStoreProvider dataStoreProvider);
    }

    interface DistributionIdGenerator{
        long id();
        void assign(Recoverable.DataBuffer dataBuffer);
    }

}
