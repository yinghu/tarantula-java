package com.icodesoftware.service;

public class ReplicationData {
    public String source;
    public int partition;
    public byte[] key;
    public byte[] value;

    public ReplicationData(String source, int partition, byte[] key, byte[] value){
        this.source = source;
        this.partition = partition;
        this.key = key;
        this.value = value;
    }
    public ReplicationData(int partition, byte[] key, byte[] value){
        this.partition = partition;
        this.key = key;
        this.value = value;
    }
}
