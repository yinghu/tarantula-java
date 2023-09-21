package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

public class SnowflakeKey implements Recoverable.Key {

    private long snowflakeId;

    public SnowflakeKey(long snowflakeId){
        this.snowflakeId = snowflakeId;
    }

    public String asString(){
        byte[] data = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putLong(snowflakeId).flip().array();
        return Base64.getEncoder().encodeToString(data);
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

