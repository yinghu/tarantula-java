package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

public class SnowflakeKey implements Recoverable.Key {

    public long snowflakeId;

    public SnowflakeKey(long snowflakeId){
        this.snowflakeId = snowflakeId;
    }

    public String asString(){
        return Long.toString(snowflakeId);
    }

    public boolean read(Recoverable.DataBuffer buffer){
        snowflakeId = buffer.readLong();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(snowflakeId==0) return false;
        buffer.writeLong(snowflakeId);
        return true;
    }
    @Override
    public String toString(){
        return asString();
    }

    @Override
    public int hashCode(){
        return Long.hashCode(snowflakeId);
    }

    public long snowflakeId(){
        return snowflakeId;
    }

    @Override
    public boolean equals(Object obj){
        SnowflakeKey r = (SnowflakeKey)obj;
        return snowflakeId == r.snowflakeId();
    }
}

