package com.icodesoftware.service;

import com.icodesoftware.DataStore;
import com.icodesoftware.Event;

public interface KeyIndexService extends ServiceProvider{

    String NAME = "KeyIndexService";

    KeyIndex lookup(String source,String key);

    void onReplicated(Event event);
    boolean createIfAbsent(KeyIndex keyIndex);

    boolean update(KeyIndex keyIndex);


    interface KeyIndexStore extends DataStore.Backup {
        String STORE_NAME_PREFIX = "tarantula_key_index_";
        String name();
        int partitionNumber();
        long count();
        long count(int partition);
    }
}
