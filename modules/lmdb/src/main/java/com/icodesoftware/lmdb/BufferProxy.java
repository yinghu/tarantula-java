package com.icodesoftware.lmdb;

import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;

public class BufferProxy implements Recoverable.DataBuffer {

    private ByteBuffer buffer;


    public BufferProxy(ByteBuffer buffer){
        this.buffer = buffer;
    }

    public Recoverable.DataBuffer writeHeader(Recoverable.DataHeader dataHeader){
        buffer.put(dataHeader.local()?(byte) 1:0).putLong(dataHeader.revision());
        buffer.putInt(dataHeader.factoryId()).putInt(dataHeader.classId());
        return this;
    }
    @Override
    public Recoverable.DataBuffer writeInt(int d) {
        buffer.putInt(d);
        return this;
    }

    @Override
    public Recoverable.DataBuffer writeLong(long l) {
        buffer.putLong(l);
        return this;
    }

    @Override
    public Recoverable.DataBuffer writeBoolean(boolean b){
        buffer.put(b?(byte) 1:0);
        return this;
    }

    public Recoverable.DataBuffer writeFloat(float f){
        buffer.putFloat(f);
        return this;
    }

    public Recoverable.DataBuffer writeDouble(double d){
        buffer.putDouble(d);
        return this;
    }

    public Recoverable.DataBuffer writeShort(short s){
        buffer.putShort(s);
        return this;
    }


    public Recoverable.DataBuffer writeByte(byte b){
        buffer.put(b);
        return this;
    }
    @Override
    public Recoverable.DataBuffer writeUTF8(String utf) {
        buffer.putInt(utf.length());
        buffer.put(utf.getBytes());
        return this;
    }

    public Recoverable.DataHeader readHeader(){
        return new LocalHeader(buffer.get()==1,buffer.getLong(),buffer.getInt(),buffer.getInt());
    }
    @Override
    public int readInt() {
        return buffer.getInt();
    }

    @Override
    public long readLong() {
        return buffer.getLong();
    }

    @Override
    public float readFloat() {
        return buffer.getFloat();
    }

    @Override
    public double readDouble() {
        return buffer.getDouble();
    }

    @Override
    public short readShort() {
        return buffer.getShort();
    }

    @Override
    public byte readByte() {
        return buffer.get();
    }

    public boolean readBoolean(){
        return buffer.get()==1;
    }

    @Override
    public String readUTF8() {
        int len = buffer.getInt();
        StringBuffer sb  = new StringBuffer();
        for(int i=0;i<len;i++){
            sb.append((char)buffer.get());
        }
        return sb.toString();
    }
}
