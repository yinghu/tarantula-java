package com.tarantula.platform.service.persistence;

import com.icodesoftware.Recoverable;
import com.icodesoftware.service.OnReplication;

public class ReplicationData implements OnReplication {

    private String nodeName;
    private int scope;
    private String source;
    private String label;

    private byte[] key;
    private byte[] value;
    private int factoryId;
    private int classId;
    private String keyAsString;
    private Recoverable recoverable;

    public ReplicationData(String nodeName,String source,String label,byte[] key, byte[] value){
        this.nodeName = nodeName;
        this.source = source;
        this.label = label;
        this.key = key;
        this.value = value;
    }
    public ReplicationData(String nodeName,byte[] key, byte[] value){
        this.nodeName = nodeName;
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

    public String label(){
        return label;
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
