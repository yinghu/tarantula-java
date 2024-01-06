package com.tarantula.platform.util;

import com.icodesoftware.util.Base64Util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.UUID;


public class SystemUtil {


    public static String toHexString(byte[] hash){
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
    public static  String accessKey(MessageDigest messageDigest,String typeId,String gameClusterId,long timestamp,String waterMark) {
        //{gameClusterId}-{hash}
        StringBuffer token = new StringBuffer(gameClusterId);
        messageDigest.reset();
        messageDigest.update(typeId.getBytes());
        messageDigest.update(gameClusterId.getBytes());
        messageDigest.update(Long.toHexString(timestamp).getBytes());//saved on game cluster id
        messageDigest.update(waterMark.getBytes());
        String hash = SystemUtil.toHexString(messageDigest.digest());
        token.append("-").append(hash).append("-"+waterMark);
        return token.toString();
    }
    public static String validAccessKey(MessageDigest messageDigest,String accessKey,String typeId,long timestamp){
        String[] sp = accessKey.split("-");
        messageDigest.reset();
        messageDigest.update(typeId.getBytes());
        messageDigest.update(sp[0].getBytes());
        messageDigest.update(Long.toHexString(timestamp).getBytes());
        messageDigest.update(sp[2].getBytes());
        String hash = SystemUtil.toHexString(messageDigest.digest());
        return hash.equals(sp[1])?sp[2]:null;
    }

    public static String hashPassword(MessageDigest messageDigest,String password) {
        messageDigest.reset();
        messageDigest.update(password.getBytes());
        return SystemUtil.toHexString(messageDigest.digest());
    }

    public static int partition(byte[] key,int partitionNumber){
        return Math.abs(Arrays.hashCode(key))%partitionNumber;
    }
    public static int partition(Object key,int partitionNumber){
        if(key instanceof Long){
            ByteBuffer byteBuffer = ByteBuffer.allocate(8).putLong((Long)key);
            return Math.abs(Arrays.hashCode(byteBuffer.array()))%partitionNumber;
        }
        return Math.abs(Arrays.hashCode(key.toString().getBytes()))%partitionNumber;
    }


    public static String oid(){
        return UUID.randomUUID().toString().replace("-","");
    }

    public static String mimeType(String path){
        String contentType = "text/html";
        if(path.endsWith(".css")){
            contentType = "text/css";
        }
        else if(path.endsWith(".html")){
            contentType = "text/html";
        }
        else if(path.endsWith(".js")){
            contentType = "text/javascript";
        }
        else if(path.endsWith(".png")){
            contentType = "image/png";
        }
        else if(path.endsWith(".jpeg")){
            contentType = "image/jpeg";
        }
        else if(path.endsWith(".jpg")){
            contentType = "image/jpeg";
        }
        else if(path.endsWith("svg")){
            contentType = "image/svg+xml";
        }
        else if(path.endsWith(".map")){
            contentType = "application/octet-stream";
        }
        else if(path.endsWith(".json")){
            contentType = "application/json";
        }
        return contentType;
    }


    public static String toBase64String(byte[] data){
        return Base64Util.toBase64String(data);
    }
    public static byte[] fromBase64String(String data){
        return Base64Util.fromBase64String(data);
    }

    public static byte[] fromPemString(String base64Key){
        String privateKeyPEM = base64Key.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll("\n", "").replace("-----END PRIVATE KEY-----", "");
        return Base64Util.fromBase64String(privateKeyPEM);
    }


}
