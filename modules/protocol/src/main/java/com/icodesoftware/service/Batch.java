package com.icodesoftware.service;



public interface Batch {

    String batchId();
    String batchKey();
    int count();
    int size();
    byte[] payload();
}
