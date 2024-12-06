package com.icodesoftware.util;

import java.nio.ByteBuffer;
import java.util.Base64;

public class Base64Util {

    public static String toBase64String(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }
    public static byte[] fromBase64String(String data){
        return Base64.getDecoder().decode(data);
    }
    public static String toBase64String(ByteBuffer buffer){
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return toBase64String(data);
    }


}
