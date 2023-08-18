package com.icodesoftware.util;

import java.nio.ByteBuffer;
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
}
