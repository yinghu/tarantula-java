package com.icodesoftware.service;

public interface Batchable {
    int size();
    BatchData[] batch();

    interface BatchData{
        byte[] key();
        byte[] value();

    }
}
