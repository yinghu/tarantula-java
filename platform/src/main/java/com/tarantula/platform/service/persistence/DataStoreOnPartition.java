package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Metadata;
import com.sleepycat.je.Database;

public class DataStoreOnPartition {
    public final int partition;
    public final String name;
    public DataStore dataStore;//set on data store ready

    public Database database;
    public Metadata metadata;
    public DataStoreOnPartition(int partition,String name){
        this.partition = partition;
        this.name = name;
    }
    public DataStoreOnPartition(int partition,Database dataStore){
        this.partition = partition;
        this.name = dataStore.getDatabaseName();
        this.database = dataStore;
        //this.metadata = new RecoverableMetadata(name,partition, Distributable.DATA_SCOPE);
    }
}
