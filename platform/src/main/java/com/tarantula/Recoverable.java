package com.tarantula;


//portable interface, which could be replicated in cluster wide or persistent in storage

import java.util.Map;

public interface Recoverable extends Distributable{

    String PATH_SEPARATOR = "/";

    String oid();
    void oid(String oid);

    String owner();
    void owner(String owner);
    //map format is back-forwarding support if keeping map key no duplicated
    //new mappings can be added in runtime to use getOrDefault on fromMap call first time
    Map<String,Object> toMap();
    void fromMap(Map<String,Object> properties);

    //binary format persistence APIs
    //Be aware it is not back-forwarding support. use map format on development stage
    byte[] toByteArray();
    void fromByteArray(byte[] data);

    boolean binary();
    void binary(boolean binary);
    //END OF binary format persistence APIs

    boolean disabled();
    void disabled(boolean disabled);

    String vertex();
    void vertex(String vertex);

    String label();
    void label(String label);

    long timestamp();
    void timestamp(long timestamp);

    //the shard version
    int version();
    void version(int version);

    //long sequence();
    //void sequence(long sequence);

    boolean onEdge();
    void onEdge(boolean onEdge);
    int getFactoryId();
    int getClassId();

    Key key();

    interface Key{
        String asString();
    }
}
