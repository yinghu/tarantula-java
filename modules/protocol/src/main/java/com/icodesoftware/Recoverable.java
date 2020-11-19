package com.icodesoftware;


//portable interface, which could be replicated in cluster wide or persistent in storage

import java.util.Map;

public interface Recoverable extends Distributable {

    String PATH_SEPARATOR = "/";

    //marked as backup operation on remote data storage
    boolean backup();

    String oid();
    void oid(String oid);

    String owner();
    void owner(String owner);

    //map format is back-forwarding support if keeping map key no duplicated
    //new mappings can be added in runtime to use getOrDefault on fromMap call first time
    Map<String,Object> toMap();
    void fromMap(Map<String,Object> properties);

    byte[] toBinary();
    void fromBinary(byte[] payload);

    boolean disabled();
    void disabled(boolean disabled);

    String label();
    void label(String label);

    long timestamp();
    void timestamp(long timestamp);

    //the shard version
    int version();
    void version(int version);


    boolean onEdge();
    void onEdge(boolean onEdge);

    int getFactoryId();
    int getClassId();

    Key key();

    interface Key{
        String asString();
    }
}
