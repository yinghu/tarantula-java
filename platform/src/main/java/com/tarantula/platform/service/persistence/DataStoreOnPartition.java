package com.tarantula.platform.service.persistence;

import com.sleepycat.je.Database;
import com.tarantula.DataStore;
import com.tarantula.Distributable;
import com.tarantula.Metadata;
import com.tarantula.platform.service.cluster.PartitionIndex;

public class DataStoreOnPartition {
    public final int partition;
    public final String name;
    public DataStore dataStore;//set on data store ready
    public PartitionIndex partitionIndex;

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
        this.metadata = new RecoverableMetadata(name,partition, Distributable.DATA_SCOPE);
    }
}
