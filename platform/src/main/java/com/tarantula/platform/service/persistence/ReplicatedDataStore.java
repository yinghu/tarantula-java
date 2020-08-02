package com.tarantula.platform.service.persistence;

import com.tarantula.DataStore;


public abstract class ReplicatedDataStore implements DataStore,DataStore.Backup {
    abstract public void close();
}
