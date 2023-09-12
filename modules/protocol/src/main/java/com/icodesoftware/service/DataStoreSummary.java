package com.icodesoftware.service;

import com.icodesoftware.Recoverable;

public interface DataStoreSummary {

    String name();

    int partitionNumber();

    long totalRecords();

    void list(DataStoreSummary.View view);

    void load(byte[] key, DataStoreSummary.View view);

    interface View{
         boolean on(ClusterProvider.Node node,Recoverable.DataHeader h, Recoverable t);
    }
}
