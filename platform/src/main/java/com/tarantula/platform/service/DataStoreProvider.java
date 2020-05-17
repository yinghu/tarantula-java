package com.tarantula.platform.service;

import com.tarantula.DataStore;

import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Map;

/**
 * Updated by yinghu on 6/28/2019.
 */
public interface DataStoreProvider extends ServiceProvider {

    int CONCURRENCY_ACCESS_LIMIT = 100;

    void configure(Map<String,String> properties);
    DataStore create(String name);
    DataStore create(String name,int partition);
    List<String> list();

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
