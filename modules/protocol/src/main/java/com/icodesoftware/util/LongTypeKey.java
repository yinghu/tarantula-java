package com.icodesoftware.util;


import com.icodesoftware.Recoverable;

public class LongTypeKey implements Recoverable.Key{

   private long longId;

    public LongTypeKey(long longId){
        this.longId = longId;
    }

    public void read(Recoverable.DataBuffer buffer){
        longId = buffer.readLong();
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(longId==0) return false;
        buffer.writeLong(longId);
        return true;
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