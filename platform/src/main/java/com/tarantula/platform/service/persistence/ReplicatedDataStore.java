package com.tarantula.platform.service.persistence;

import com.tarantula.DataStore;


public abstract class ReplicatedDataStore implements DataStore {
    abstract public void close();
}
