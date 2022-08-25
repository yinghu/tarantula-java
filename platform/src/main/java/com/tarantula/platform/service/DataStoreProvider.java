package com.tarantula.platform.service;


import com.icodesoftware.DataStore;
import com.icodesoftware.service.ServiceProvider;
import com.tarantula.platform.service.persistence.Node;
import com.tarantula.platform.service.persistence.BackupProvider;

import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;


public interface DataStoreProvider extends ServiceProvider {

    //int CONCURRENCY_ACCESS_LIMIT = 17;

    void configure(Map<String,String> properties);
    void addBackupProvider(BackupProvider shardingProvider);

    Node node();
    //create none-partitioned integration scope data store
    DataStore create(String name);

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

}
