package com.tarantula.platform.service.persistence;

import com.tarantula.DataStore;
import com.tarantula.platform.event.MapStoreSyncEvent;

public abstract class ReplicatedDataStore implements DataStore {
    abstract public void close();
    abstract public void onReplication(MapStoreSyncEvent event);
    abstract public int scope();
    abstract public void put(byte[] key,byte[] value);
}
