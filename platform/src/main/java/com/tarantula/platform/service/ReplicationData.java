package com.tarantula.platform.service;

public class ReplicationData {
    public String source;
    public int partition;
    public byte[] key;
    public byte[] value;
    public ReplicationData(String source,byte[] key, byte[] value){
        this.source = source;
        this.key = key;
        this.value = value;
    }
    public ReplicationData(byte[] key, byte[] value){
        this.key = key;
        this.value = value;
    }
}
