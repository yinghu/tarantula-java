package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;

public class LongCompositeKey implements Recoverable.Key {

    private long from;
    private long to;

    public LongCompositeKey(long from, long to){
        this.from = from;
        this.to = to;
    }

    public String asString(){
        return Base64.getEncoder().encodeToString(asBinary());
    }

    @Override
    public byte[] asBinary() {
        return ByteBuffer.allocate(16).order(ByteOrder.nativeOrder()).putLong(from).putLong(to).flip().array();
    }

    public boolean read(Recoverable.DataBuffer buffer){
        from = buffer.readLong();
        to = buffer.readLong();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        buffer.writeLong(from);
        buffer.writeLong(to);
        return true;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(new long[]{from,to});
    }

    public long from(){
        return from;
    }
    public long to(){
        return to;
    }

    @Override
    public boolean equals(Object obj){
        LongCompositeKey r = (LongCompositeKey)obj;
        return from == r.from() && to == r.to();
    }

    public static LongCompositeKey from(long from, long to){
        return new LongCompositeKey(from,to);
    }
}

