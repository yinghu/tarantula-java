package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;


public class MemorySegmentProxy implements Recoverable.DataBuffer {


    private final MemorySegment memorySegment;
    private long offset;
    private MemorySegmentProxy(MemorySegment memorySegment){
        this.memorySegment = memorySegment;
        this.memorySegment.fill((byte)0);
        this.offset = 0;
    }


    public Recoverable.DataBuffer writeHeader(Recoverable.DataHeader dataHeader){
        memorySegment.set(ValueLayout.JAVA_LONG,offset,dataHeader.revision());
        offset += 8;
        memorySegment.set(ValueLayout.JAVA_INT,offset,dataHeader.factoryId());
        offset += 4;
        memorySegment.set(ValueLayout.JAVA_INT,offset,dataHeader.classId());
        offset += 4;
        return this;
    }
    @Override
    public Recoverable.DataBuffer writeInt(int d) {
        memorySegment.set(ValueLayout.JAVA_INT,offset,d);
        offset += 4;
        return this;
    }

    @Override
    public Recoverable.DataBuffer writeLong(long l) {
        memorySegment.set(ValueLayout.JAVA_LONG,offset,l);
        offset += 8;
        return this;
    }

    @Override
    public Recoverable.DataBuffer writeBoolean(boolean b){
        memorySegment.set(ValueLayout.JAVA_BOOLEAN,offset,b);
        offset += 1;
        return this;
    }

    public Recoverable.DataBuffer writeFloat(float f){
        memorySegment.set(ValueLayout.JAVA_FLOAT,offset,f);
        offset += 4;
        return this;
    }

    public Recoverable.DataBuffer writeDouble(double d){
        memorySegment.set(ValueLayout.JAVA_DOUBLE,offset,d);
        offset += 8;
        return this;
    }

    public Recoverable.DataBuffer writeShort(short s){
        memorySegment.set(ValueLayout.JAVA_SHORT,offset,s);
        offset += 2;
        return this;
    }


    public Recoverable.DataBuffer writeByte(byte b){
        memorySegment.set(ValueLayout.JAVA_BYTE,offset,b);
        offset += 1;
        return this;
    }
    @Override
    public Recoverable.DataBuffer writeUTF8(String utf) {
        int len = 3;
        if(utf != null && utf.length() > 0){
            len = utf.length();
        }

        return this;
    }

    public Recoverable.DataHeader readHeader(){
        long revision = memorySegment.get(ValueLayout.JAVA_LONG,offset);
        offset += 8;
        int factoryId = memorySegment.get(ValueLayout.JAVA_INT,offset);
        offset += 4;
        int classId = memorySegment.get(ValueLayout.JAVA_INT,offset);
        offset += 4;
        return new LocalHeader(revision,factoryId,classId);
    }
    @Override
    public int readInt() {
        int v = memorySegment.get(ValueLayout.JAVA_INT,offset);
        offset += 4;
        return v;
    }

    @Override
    public long readLong() {
        long v = memorySegment.get(ValueLayout.JAVA_LONG,offset);
        offset += 8;
        return v;
    }

    @Override
    public float readFloat() {
        float v = memorySegment.get(ValueLayout.JAVA_FLOAT,offset);
        offset += 4;
        return v;
    }

    @Override
    public double readDouble() {
        double v = memorySegment.get(ValueLayout.JAVA_DOUBLE,offset);
        offset += 8;
        return v;
    }

    @Override
    public short readShort() {
        short v = memorySegment.get(ValueLayout.JAVA_SHORT,offset);
        offset += 2;
        return v;
    }

    @Override
    public byte readByte() {
        byte v = memorySegment.get(ValueLayout.JAVA_BYTE,offset);
        offset += 1;
        return v;
    }

    public boolean readBoolean(){
        boolean v = memorySegment.get(ValueLayout.JAVA_BOOLEAN,offset);
        offset += 1;
        return v;
    }

    @Override
    public String readUTF8() {
        StringBuffer sb  = new StringBuffer();

        String ret = sb.toString();
        return ret.equals(Recoverable.UTF_NULL)?null:ret;
    }

    public static Recoverable.DataBuffer memory(MemorySegment memorySegment){
        return new MemorySegmentProxy(memorySegment);
    }


    public byte[] array(){
       return null;
    }

    public ByteBuffer src(){
        return null;
    }
    public ByteBuffer flip(){
        offset = 0;
        return null;//memorySegment.asByteBuffer();
    }
    public ByteBuffer rewind(){
        offset = 0;
        return null;//memorySegment.asByteBuffer();
    }
    public ByteBuffer clear(){
        memorySegment.fill((byte)0);
        offset = 0;
        return null;
    }
    public boolean hasRemaining(){
        return false;
    }

    public int remaining(){
        return 0;
    }

    public void position(int position){

    }

}
