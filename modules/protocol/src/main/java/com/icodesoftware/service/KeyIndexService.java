package com.icodesoftware.service;

import com.icodesoftware.DataStore;

public interface KeyIndexService extends ServiceProvider{

    String NAME = "KeyIndexService";

    KeyIndex lookup(String source,String key);


    interface KeyIndexStore extends DataStore.Backup {
        String STORE_NAME_PREFIX = "tarantula_key_index_";
        String name();
        int partitionNumber();
        long count();
        long count(int partition);
    }
}
