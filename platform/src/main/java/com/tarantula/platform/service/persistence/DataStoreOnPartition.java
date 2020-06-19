package com.tarantula.platform.service.persistence;

import com.sleepycat.je.Database;
import com.tarantula.DataStore;
import com.tarantula.platform.service.cluster.PartitionIndex;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DataStoreOnPartition {
    public final int partition;
    public final String name;
    public DataStore dataStore;//set on data store ready
    public PartitionIndex partitionIndex;
    public Database database;
    public final AtomicBoolean enabled;
    public final AtomicBoolean local;
    public final AtomicLong count;
    public final AtomicLong total;
    public DataStoreOnPartition(int partition,String name){
        this.partition = partition;
        this.name = name;
        this.enabled = new AtomicBoolean(false);
        this.local = new AtomicBoolean(false);
        this.count = new AtomicLong(0);
        this.total = new AtomicLong(0);
    }
    public DataStoreOnPartition(int partition,Database dataStore){
        this.partition = partition;
        this.name = dataStore.getDatabaseName();
        this.database = dataStore;
        this.enabled = new AtomicBoolean(false);
        this.local = new AtomicBoolean(false);
        this.count = new AtomicLong(0);
        this.total = new AtomicLong(0);
    }
    public void reset(){
        enabled.set(false);
        count.set(0);
        local.set(false);
        total.set(0);
    }
}
