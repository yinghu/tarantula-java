package com.icodesoftware.util;

import com.icodesoftware.service.DeploymentServiceProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CipherUtil {

    public static Cipher encrypt(byte[] key) throws Exception{
        IvParameterSpec iv = new IvParameterSpec(key);
        SecretKey secretKey = new SecretKeySpec(key, DeploymentServiceProvider.SERVER_KEY_SPEC);
        Cipher cipher = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
        cipher.init(Cipher.ENCRYPT_MODE,secretKey,iv);
        return cipher;
    }
    public static Cipher decrypt(byte[] key) throws Exception{
        IvParameterSpec iv = new IvParameterSpec(key);
        SecretKey secretKey = new SecretKeySpec(key, DeploymentServiceProvider.SERVER_KEY_SPEC);
        Cipher cipher = Cipher.getInstance(DeploymentServiceProvider.CIPHER_NAME_CBC_PKC5PADDING);
        cipher.init(Cipher.DECRYPT_MODE,secretKey,iv);
        return cipher;
    }

    public static byte[] key(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] _key = new byte[DeploymentServiceProvider.KEY_SIZE];
        secureRandom.nextBytes(_key);
        return _key;
    }

    public static String toBase64Key(){
        return Base64.getEncoder().encodeToString(key());
    }
    public static String toBase64Key(byte[] key){
        return Base64.getEncoder().encodeToString(key);
    }
    public static byte[] fromBase64Key(String base64Key){
        return Base64.getDecoder().decode(base64Key);
    }

    public static int cipherSize(int plainSize){
        return DeploymentServiceProvider.KEY_SIZE - (plainSize % DeploymentServiceProvider.KEY_SIZE);
    }
}
