package com.icodesoftware.util;

import com.icodesoftware.service.TokenValidatorProvider;

import java.security.MessageDigest;

public class MessageDigestUtil {

    private static MessageDigest _messageDigest;

    static {
        try{
            _messageDigest = MessageDigest.getInstance(TokenValidatorProvider.MDA);
        }catch (Exception ex){
            throw new RuntimeException(TokenValidatorProvider.MDA+" not supported");
        }
    }

    public static String hash(String source){
        try{
            MessageDigest clone = (MessageDigest)_messageDigest.clone();
            clone.update(source.getBytes());
            return toHexString(clone.digest());
        }catch (Exception ex){
            throw new RuntimeException("message digest clone not supported");
        }
    }

    private static String toHexString(byte[] hash){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hash.length; i++){
            int v = hash[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

}
