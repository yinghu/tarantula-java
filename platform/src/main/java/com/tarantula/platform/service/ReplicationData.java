package com.tarantula.platform.service;

import com.icodesoftware.service.OnReplication;

public class ReplicationData implements OnReplication {
    public String source;
    public int partition;
    public byte[] key;
    public byte[] value;
    public ReplicationData(String source,byte[] key, byte[] value){
        this.source = source;
        this.key = key;
        this.value = value;
    }
    public ReplicationData(int partition,byte[] key, byte[] value){
        this.partition = partition;
        this.key = key;
        this.value = value;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public int partition() {
        return partition;
    }

    @Override
    public byte[] key() {
        return key;
    }

    @Override
    public byte[] value() {
        return value;
    }
}
