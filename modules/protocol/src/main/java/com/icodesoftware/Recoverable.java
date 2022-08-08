package com.icodesoftware;


import java.util.Map;

public interface Recoverable extends Distributable,JsonSerializable {

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

    //the data store version; never use it in application
    long revision();
    void revision(long revision);


    boolean onEdge();
    void onEdge(boolean onEdge);

    int getFactoryId();
    int getClassId();

    Key key();

    interface Key{
        String asString();
    }
}
