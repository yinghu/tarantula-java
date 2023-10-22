package com.icodesoftware.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BufferUtil {
    public static byte[] toArray(ByteBuffer buffer){
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }
    public static boolean equals(ByteBuffer source,ByteBuffer target){
        if(source.remaining() != target.remaining()) return false;
        byte[] sdata = new byte[source.remaining()];
        byte[] tdata = new byte[source.remaining()];
        source.get(sdata);
        target.get(tdata);
        return Arrays.equals(sdata,tdata);
    }
    public static long toLong(byte[] data){
        return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).put(data).flip().getLong();
    }
    public static byte[] fromLong(long data){
        return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putLong(data).flip().array();
    }
    public static byte[] fromLong(String data){
        return ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putLong(Long.parseLong(data)).flip().array();
    }
}
