package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class DataBufferKey implements Recoverable.Key {

    public final Recoverable.DataBuffer key;

    private DataBufferKey(Recoverable.DataBuffer key){
        this.key = key;
    }

    public String asString(){
        return Base64.getEncoder().encodeToString(asBinary());
    }

    @Override
    public byte[] asBinary() {
        return key.array();
    }

    public boolean read(Recoverable.DataBuffer buffer){

        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(key==null) return false;
        while (key.hasRemaining()){
            buffer.writeByte(key.readByte());
        }
        return true;
    }
    @Override
    public String toString(){
        return asString();
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(key);
    }

    @Override
    public boolean equals(Object obj){
        DataBufferKey r = (DataBufferKey)obj;
        return Arrays.equals(r.asBinary(),asBinary());
    }

    public static DataBufferKey from(Recoverable.DataBuffer key){
        return new DataBufferKey(key);
    }
}

