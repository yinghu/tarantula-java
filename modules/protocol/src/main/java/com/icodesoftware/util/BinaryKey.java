package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

public class BinaryKey implements Recoverable.Key {

    public byte[] key;

    public BinaryKey(byte[] key){
        this.key = key;
    }

    public String asString(){
        return "binary key";
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
        return "Owner access key ["+key+"]";
    }

    @Override
    public int hashCode(){
        return this.key.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        BinaryKey r = (BinaryKey)obj;
        return key.equals(r.key);
    }
}

