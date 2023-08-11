package com.tarantula.platform.service;

import com.icodesoftware.DataStore;

public class DataStoreSummary implements DataStore.Summary {

    public String  name;
    public int partitionNumber;
    public long totalRecords;
    public DataStore dataStore;
    @Override
    public String name() {
        return name;
    }

    @Override
    public int partitionNumber() {
        return partitionNumber;
    }

    @Override
    public long totalRecords() {
        return totalRecords;
    }

    public DataStore dataStore(){
        return dataStore;
    }


    public void list(DataStore.Binary binary){
        dataStore.backup().list(binary);
    }

    public void load(byte[] key, DataStore.Binary binary){
        binary.on(key,dataStore.backup().get(key));
    }
}
