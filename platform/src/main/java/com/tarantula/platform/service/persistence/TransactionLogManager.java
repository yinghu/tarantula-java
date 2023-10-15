package com.tarantula.platform.service.persistence;

import com.icodesoftware.service.DataStoreProvider;

import java.util.List;

public class TransactionLogManager{

    private final DataStoreProvider dataStoreProvider;
    public TransactionLogManager(final  DataStoreProvider dataStoreProvider){
        this.dataStoreProvider = dataStoreProvider;
    }

    public void log(){

    }

    public List<TransactionLog> committed(long transactionId){
        return null;
    }
}
