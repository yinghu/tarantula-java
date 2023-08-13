package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.OnReplication;

public class ReplicationData implements OnReplication {

    private String nodeName;
    private int scope;
    private String source;
    private int partition;
    private byte[] key;
    private byte[] value;
    private int factoryId;
    private int classId;
    private String keyAsString;
    private Recoverable recoverable;

    public ReplicationData(String nodeName,String source,byte[] key, byte[] value){
        this.source = source;
        this.key = key;
        this.value = value;
    }
    public ReplicationData(String nodeName,int partition,byte[] key, byte[] value){
        this.nodeName = nodeName;
        this.partition = partition;
        this.key = key;
        this.value = value;
    }

    public ReplicationData(String source,String key,Recoverable value){
        this.source = source;
        this.keyAsString = key;
        this.recoverable = value;
    }
    public ReplicationData(byte[] payload){
        this.value = payload;
    }

    @Override
    public int scope(){
        return this.scope;
    }

    public String nodeName(){
        return nodeName;
    }
    @Override
    public String source() {
        return source;
    }

    @Override
    public int partition() {
        return partition;
    }

    public int factoryId(){
        return factoryId;
    }
    public int classId(){
        return classId;
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
