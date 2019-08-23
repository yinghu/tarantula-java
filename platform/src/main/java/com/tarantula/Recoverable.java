package com.tarantula;


//portable interface, which could be replicated in cluster wide or persistent in storage

import java.util.Map;

public interface Recoverable extends Distributable{

    String PATH_SEPARATOR = "/";

    String oid();
    void oid(String oid);

    String owner();
    void owner(String owner);

    Map<String,Object> toMap();
    void fromMap(Map<String,Object> properties);

    byte[] toByteArray();
    void fromByteArray(byte[] data);

    boolean binary();
    void binary(boolean binary);

    boolean disabled();
    void disabled(boolean disabled);

    String vertex();
    void vertex(String vertex);

    String label();
    void label(String label);

    long timestamp();
    void timestamp(long timestamp);

    int version();
    void version(int version);

    long sequence();
    void sequence(long sequence);

    boolean onEdge();
    void onEdge(boolean onEdge);
    int getFactoryId();
    int getClassId();

    Key key();

    void dataStore(DataStore dataStore);

    void onUpdate();

    interface Key{
        String asString();
    }
}
