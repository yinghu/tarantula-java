package com.tarantula.platform.service.persistence;


import com.icodesoftware.Closable;
import com.icodesoftware.DataStore;

public interface ReplicatedDataStore extends DataStore,DataStore.Backup, Closable {
}
