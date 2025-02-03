package com.icodesoftware;

import java.nio.ByteBuffer;

public interface Recoverable extends Distributable,JsonSerializable,Bufferable,Validatable {

    String PATH_SEPARATOR = "/";

    Key ownerKey();
    void ownerKey(Key ownerKey);

    String owner();
    void owner(String owner);

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

    interface Key extends Bufferable,Validatable{
        String asString();
        byte[] asBinary();
    }


    interface DataHeader{

        int SIZE = 16;

        long revision();

        int factoryId();

        int classId();

        void update(long revisionDelta);
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

        DataBuffer write(DataBuffer src);
        void read(DataBuffer dest);

        byte[] array();
        ByteBuffer src();
        ByteBuffer flip();
        ByteBuffer rewind();
        ByteBuffer clear();
        boolean hasRemaining();
        int remaining();
        void position(int position);
        boolean full();
        int size();
        boolean direct();
    }

    interface DataBufferPair extends Resettable,AutoCloseable{
        DataBuffer key();
        DataBuffer value();

        @Override
        void close();
    }
}
