package com.tarantula.platform.service;


import com.icodesoftware.DataStore;

import com.icodesoftware.service.ServiceProvider;

import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;


public interface DataStoreProvider extends ServiceProvider {

    String NAME = "tarantula";


    void configure(Map<String,Object> properties);

    //void registerBackupProvider(int scope, BackupProvider mapStoreListener);

    //create none-partitioned integration scope data store
    DataStore createAccessIndexDataStore(String name);

    //create none-partitioned local scope data store
    DataStore createKeyIndexDataStore(String name);
    //create partitioned data scope data store
    DataStore create(String name,int partition);

    List<String> list();
    boolean existed(String name);

    //incremental back up
    void backup(int scope);

    //full back up with callback recover
    void backup(int scope,OnBackup backup);

    //recover from incremental back up
    void recover(int scope,OnBackup backup);

    interface OnBackup{
        void on(String fName,int fSize,ReadableByteChannel in);
    }

    interface OnStart{
        void on(DataStoreProvider dataStoreProvider);
    }

}
