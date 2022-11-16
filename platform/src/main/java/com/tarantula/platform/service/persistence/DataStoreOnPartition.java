package com.tarantula.platform.service.persistence;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.Metadata;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class DataStoreOnPartition {
    public final int partition;
    public final String name;
    public DataStore dataStore;//set on data store ready

    public Metadata metadata;

    private ConcurrentHashMap<byte[],Boolean> locks = new ConcurrentHashMap<>();
    public DataStoreOnPartition(int partition,String name){
        this.partition = partition;
        this.name = name;
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
