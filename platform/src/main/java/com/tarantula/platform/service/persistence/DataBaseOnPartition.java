package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.Metadata;
import com.sleepycat.je.Database;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class DataBaseOnPartition {
    public final int partition;
    public final String name;

    public Database database;
    public Metadata metadata;

    private ConcurrentHashMap<byte[],Boolean> locks = new ConcurrentHashMap<>();

    public DataBaseOnPartition(int partition, Database dataStore){
        this.partition = partition;
        this.name = dataStore.getDatabaseName();
        this.database = dataStore;
    }
    public boolean lock(byte[] key, Callable<Boolean> runnable){
        boolean[] ret = {false};
        locks.compute(key,(k,v)->{
            try{
                ret[0] = runnable.call();
            }catch (Exception ex){
                //ignore
            }
            return null;
        });
        return ret[0];
    }


}
