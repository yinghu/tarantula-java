package com.icodesoftware.util;

import com.icodesoftware.Recoverable;

import java.io.DataInputStream;
import java.io.InputStream;

public class InputStreamDataBufferProxy extends IOStreamDataBuffer{

    private final DataInputStream src;

    private InputStreamDataBufferProxy(InputStream inputStream){
        this.src = new DataInputStream(inputStream);
    }

    public Recoverable.DataHeader readHeader(){
        try{
            return new LocalHeader(src.readLong(),src.readInt(),src.readInt());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public String readUTF8(){
        try{
            String utf = src.readUTF();
            return utf.equals(Recoverable.UTF_NULL)?null:utf;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int readInt() {
        try{
            return src.readInt();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean readBoolean() {
        try{
            return src.readBoolean();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public long readLong() {
        try{
            return src.readLong();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public float readFloat() {
        try{
            return src.readFloat();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public double readDouble() {
        try{
            return src.readDouble();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public short readShort() {
        try{
            return src.readShort();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public byte readByte() {
        try{
            return src.readByte();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    public void read(Recoverable.DataBuffer dest){
        try{
            while (!dest.full()){
                dest.writeByte(src.readByte());
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    public static Recoverable.DataBuffer proxy(InputStream src){
        return new InputStreamDataBufferProxy(src);
    }

}
