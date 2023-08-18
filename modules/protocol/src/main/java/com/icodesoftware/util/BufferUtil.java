package com.icodesoftware.util;

import java.nio.ByteBuffer;

public class BufferUtil {
    public static byte[] toArray(ByteBuffer buffer){
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }
}
