package com.icodesoftware.util;

import java.util.Base64;

public class Base64Util {

    public static String toBase64String(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }
    public static byte[] fromBase64String(String data){
        return Base64.getDecoder().decode(data);
    }
}
