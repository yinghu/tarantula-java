package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;

public class IntegerRangeKey implements Recoverable.Key {

    private int from;
    private int to;

    public IntegerRangeKey(int from,int to){
        this.from = from;
        this.to = to;
    }

    public String asString(){
        return Base64.getEncoder().encodeToString(asBinary());
    }

    @Override
    public byte[] asBinary() {
        return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putInt(from).putInt(to).flip().array();
    }

    public boolean read(Recoverable.DataBuffer buffer){
        from = buffer.readInt();
        to = buffer.readInt();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        buffer.writeInt(from);
        buffer.writeInt(to);
        return true;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(new int[]{from,to});
    }

    public int from(){
        return from;
    }
    public int to(){
        return to;
    }

    @Override
    public boolean equals(Object obj){
        IntegerRangeKey r = (IntegerRangeKey)obj;
        return from == r.from() && to == r.to();
    }

    public static IntegerRangeKey from(int from,int to){
        return new IntegerRangeKey(from,to);
    }
}

