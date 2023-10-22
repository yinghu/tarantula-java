package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

public class BinaryKey implements Recoverable.Key {

    public byte[] key;

    public BinaryKey(byte[] key){
        this.key = key;
    }

    public BinaryKey(){
    }

    public String asString(){
        return Base64.getEncoder().encodeToString(key);
    }

    @Override
    public byte[] asBinary() {
        return key;
    }

    public boolean read(Recoverable.DataBuffer buffer){
        key = buffer.array();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(key==null) return false;
        for(byte b : key){
            buffer.writeByte(b);
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
        BinaryKey r = (BinaryKey)obj;
        return Arrays.equals(key,r.key);
    }
}

