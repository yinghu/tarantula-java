package com.icodesoftware.lmdb;

import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;

public class BufferProxy implements Recoverable.DataBuffer {

    private ByteBuffer buffer;

    public boolean local;
    public long revision;

    public BufferProxy(ByteBuffer buffer){
        this.buffer = buffer;
    }
    @Override
    public void writeInt(int d) {
        buffer.putInt(d);
    }

    @Override
    public void writeLong(long l) {
        buffer.putLong(l);
    }

    @Override
    public void writeBoolean(boolean b){
        buffer.put(b?(byte) 1:0);
    }

    public void writeFloat(float f){
        buffer.putFloat(f);
    }

    public void writeDouble(double d){
        buffer.putDouble(d);
    }

    public void writeShort(short s){
        buffer.putShort(s);
    }


    public void writeByte(){}
    @Override
    public void writeUTF8(String utf) {
        buffer.putInt(utf.length());
        buffer.put(utf.getBytes());
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
