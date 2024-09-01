package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

public class IntegerKey implements Recoverable.Key {

    private int integerId;

    public IntegerKey(int integerId){
        this.integerId = integerId;
    }

    public String asString(){
        return Base64.getEncoder().encodeToString(asBinary());
    }

    @Override
    public byte[] asBinary() {
        return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(integerId).flip().array();
    }

    public boolean read(Recoverable.DataBuffer buffer){
        integerId = buffer.readInt();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        if(integerId <= 0) return false;
        buffer.writeInt(integerId);
        return true;
    }

    @Override
    public int hashCode(){
        return Integer.hashCode(integerId);
    }

    public int integerId(){
        return integerId;
    }

    @Override
    public boolean equals(Object obj){
        IntegerKey r = (IntegerKey)obj;
        return integerId == r.integerId();
    }

    public static IntegerKey from(int key){
        return new IntegerKey(key);
    }
}

