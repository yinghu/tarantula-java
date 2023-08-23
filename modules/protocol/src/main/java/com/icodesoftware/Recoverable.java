package com.icodesoftware;

import java.util.Map;

public interface Recoverable extends Distributable,JsonSerializable {

    String PATH_SEPARATOR = "/";

    //marked as backup operation on remote data storage
    boolean backup();

    String oid();
    void oid(String oid);

    long id();
    void id(long id);

    Key ownerKey();
    void ownerKey(Key ownerKey);

    String owner();
    void owner(String owner);

    //map format is back-forwarding support if keeping map key no duplicated
    //new mappings can be added in runtime to use getOrDefault on fromMap call first time
    Map<String,Object> toMap();
    void fromMap(Map<String,Object> properties);

    byte[] toBinary();
    void fromBinary(byte[] payload);

    default void read(DataBuffer buffer){}
    default void write(DataBuffer buffer){}


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
        default byte[] asBinary(){ return null;}
    }


    interface DataHeader{
        boolean local();
        long revision();

        int factoryId();

        int classId();

        void update(boolean local,long revisionDelta);
    }

    interface DataBuffer{

        DataBuffer writeHeader(DataHeader header);
        DataBuffer writeInt(int i);
        DataBuffer writeLong(long l);

        DataBuffer writeFloat(float f);

        DataBuffer writeDouble(double d);

        DataBuffer writeShort(short s);

        DataBuffer writeBoolean(boolean b);

        DataBuffer writeByte(byte b);

        DataBuffer writeUTF8(String utf);

        DataHeader readHeader();

        int readInt();

        boolean readBoolean();

        long readLong();

        float readFloat();

        double readDouble();

        short readShort();

        byte readByte();
        String readUTF8();
    }
}
