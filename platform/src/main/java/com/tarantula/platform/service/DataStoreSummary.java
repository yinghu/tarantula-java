package com.tarantula.platform.service;

import com.icodesoftware.DataStore;

public class DataStoreSummary implements DataStore.Summary {

    public String  name;
    public int partitionNumber;
    public long totalRecords;

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
}
