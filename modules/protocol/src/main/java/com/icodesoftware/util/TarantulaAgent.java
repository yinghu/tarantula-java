package com.icodesoftware.util;

import com.icodesoftware.service.ClusterProvider;

import javax.crypto.Cipher;

public class TarantulaAgent implements ClusterProvider.HomingAgent {

    public boolean enabled;
    public String host;
    public String accessKey;
    public String encryptionKey;

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public String accessKey() {
        return accessKey;
    }

    @Override
    public String encryptionKey() {
        return encryptionKey;
    }

    public byte[] encrypt(byte[] data){
        try{
            Cipher cipher = CipherUtil.encrypt(CipherUtil.fromBase64Key(encryptionKey));
            return cipher.doFinal(data);
        }catch (Exception ex){
            throw new RuntimeException("encrypt error",ex);
        }
    }
    public byte[] decrypt(byte[] data){
        try{
            Cipher cipher = CipherUtil.decrypt(CipherUtil.fromBase64Key(encryptionKey));
            return cipher.doFinal(data);
        }catch (Exception ex){
            throw new RuntimeException("decrypt error",ex);
        }
    }
}
