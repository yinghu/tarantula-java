package com.icodesoftware.util;
import com.icodesoftware.Recoverable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;

public class IntegerKey implements Recoverable.Key {

    private int key;


    public IntegerKey(int key){
        this.key = key;
    }

    public String asString(){
        return Base64.getEncoder().encodeToString(asBinary());
    }

    @Override
    public byte[] asBinary() {
        return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putInt(key).array();
    }

    public boolean read(Recoverable.DataBuffer buffer){
        key = buffer.readInt();
        return true;
    }
    public boolean write(Recoverable.DataBuffer buffer){
        buffer.writeInt(key);
        return true;
    }

    @Override
    public int hashCode(){
        return Arrays.hashCode(new int[]{key});
    }

    public int key(){
        return key;
    }


    @Override
    public boolean equals(Object obj){
        IntegerKey r = (IntegerKey)obj;
        return key == r.key();
    }

    public static IntegerKey from(int key){
        return new IntegerKey(key);
    }
}

