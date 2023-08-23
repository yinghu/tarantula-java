package com.icodesoftware.util;


import com.icodesoftware.Recoverable;

public class LongTypeKey implements Recoverable.Key{

   private long longId;

    public LongTypeKey(long longId){
        this.longId = longId;
    }
    public byte[] asBinary(){
        return null;
    }
    public String asString(){
        return Long.toString(longId);
    }
    @Override
    public String toString(){
        return asString();
    }
    @Override
    public boolean equals(Object obj){
        return this.asString().equals(((LongTypeKey)obj).asString());
    }
    @Override
    public int hashCode(){
        return this.asString().hashCode();
    }
}