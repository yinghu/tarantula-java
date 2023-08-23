package com.icodesoftware.util;


import com.icodesoftware.Recoverable;

public class LongTypeKey implements Recoverable.Key{

   private long longId;

    public LongTypeKey(long longId){
        this.longId = longId;
    }

    public boolean read(Recoverable.DataBuffer buffer){
        longId = buffer.readLong();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(longId==0) return false;
        buffer.writeLong(longId);
        return true;
    }

    @Override
    public String toString(){
        return Long.toString(longId);
    }

    public long id(){
        return longId;
    }
    @Override
    public boolean equals(Object obj){
        return this.longId==(((LongTypeKey)obj).id());
    }
    @Override
    public int hashCode(){
        return Long.hashCode(this.longId);
    }
}