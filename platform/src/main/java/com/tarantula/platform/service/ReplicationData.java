package com.tarantula.platform.service;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.OnReplication;

public class ReplicationData implements OnReplication {
    public int scope;
    public String source;
    public int partition;
    public byte[] key;
    public byte[] value;

    public String keyAsString;
    public Recoverable recoverable;

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

    public ReplicationData(String source,String key,Recoverable value){
        this.source = source;
        this.keyAsString = key;
        this.recoverable = value;
    }

    @Override
    public int scope(){
        return this.scope;
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

    public String keyAsString(){
        return keyAsString;
    }
    public Recoverable recoverable(){
        return recoverable;
    }

}
