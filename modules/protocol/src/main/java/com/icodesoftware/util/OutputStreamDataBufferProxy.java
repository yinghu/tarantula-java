package com.icodesoftware.util;


import com.icodesoftware.Recoverable;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class OutputStreamDataBufferProxy extends IOStreamDataBuffer{

    private final DataOutputStream dest;

    private OutputStreamDataBufferProxy(OutputStream outputStream){
        this.dest = new DataOutputStream(outputStream);
    }

    public Recoverable.DataBuffer writeHeader(Recoverable.DataHeader header){
        try{
            dest.writeLong(header.revision());
            dest.writeInt(header.factoryId());
            dest.writeInt(header.classId());
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    @Override
    public Recoverable.DataBuffer writeInt(int value) {
        try{
            dest.writeInt(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Recoverable.DataBuffer writeLong(long value) {
        try{
            dest.writeLong(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Recoverable.DataBuffer writeFloat(float value) {
        try{
            dest.writeFloat(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Recoverable.DataBuffer writeDouble(double value) {
        try{
            dest.writeDouble(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Recoverable.DataBuffer writeShort(short value) {
        try{
            dest.writeShort(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Recoverable.DataBuffer writeBoolean(boolean value) {
        try{
            dest.writeBoolean(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Recoverable.DataBuffer writeByte(byte value) {
        try{
            dest.writeByte(value);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public Recoverable.DataBuffer writeUTF8(String utf){
        try{
            dest.writeUTF(utf);
            return this;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    public static Recoverable.DataBuffer proxy(OutputStream dest){
        return new OutputStreamDataBufferProxy(dest);
    }

}
