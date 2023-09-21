package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

public class NaturalKey implements Recoverable.Key {

    private String key;

    public NaturalKey(String key){
        this.key = key;
    }

    public String asString(){
        byte[] data = ByteBuffer.allocate(key.length()+4).order(ByteOrder.nativeOrder()).putInt(key.length()).put(key.getBytes()).flip().array();
        return Base64.getEncoder().encodeToString(data);
    }

    public boolean read(Recoverable.DataBuffer buffer){
        key = buffer.readUTF8();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(key==null) return false;
        buffer.writeUTF8(key);
        return true;
    }
    @Override
    public String toString(){
        return key;
    }

    @Override
    public int hashCode(){
        return this.key.hashCode();
    }

    @Override
    public boolean equals(Object obj){
        NaturalKey r = (NaturalKey)obj;
        return key.equals(r.key);
    }
}

