package com.tarantula.platform.util;

import com.icodesoftware.OnSession;
import com.icodesoftware.protocol.GameModule;
import com.icodesoftware.protocol.ValidationUtil;
import com.icodesoftware.service.ApplicationPreSetup;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.OnSessionTrack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.*;
import java.util.Arrays;

import java.util.Base64;
import java.util.UUID;


public class SystemUtil {


    public static String toHexString(byte[] hash){
        return ValidationUtil.toHexString(hash);
    }
    public static String ticket(MessageDigest messageDigest, String systemId, int stub, int durationSeconds,String waterMark) {
        LocalDateTime _st = LocalDateTime.now().plusSeconds(durationSeconds);
        long end = TimeUtil.toUTCMilliseconds(_st);
        StringBuffer _ticket = new StringBuffer();
        _ticket.append("tarantula").append(" ").append(end).append(" ");
        messageDigest.reset();
        messageDigest.update(systemId.getBytes());
        messageDigest.update(Integer.toHexString(stub).getBytes());
        messageDigest.update(Long.toHexString(end).getBytes());
        String hash = SystemUtil.toHexString(messageDigest.digest());
        _ticket.append(hash).append(" ").append(waterMark);
        return _ticket.toString();
    }

    public static String validTicket(MessageDigest messageDigest,String systemId,int stub,String ticket){
        return ValidationUtil.validTicket(messageDigest,systemId,stub,ticket);
    }
    public static  String token(MessageDigest messageDigest, String systemId,int stub,int timeoutMinutes,String mark,String index) {
        //{systemId} {ticket}-{routing}-{stub}-{cid}-{start}-{hash}
        //ticket=> {tarantula} {stub} {end} {hash}
        StringBuffer token = new StringBuffer(systemId);
        messageDigest.reset();
        messageDigest.update(systemId.getBytes());
        messageDigest.update(Integer.toHexString(stub).getBytes());
        long start = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        messageDigest.update(Long.toHexString(start).getBytes());
        messageDigest.update(index.getBytes());
        String hash = SystemUtil.toHexString(messageDigest.digest());
        String ticket = SystemUtil.ticket(messageDigest,systemId,stub,timeoutMinutes*60,mark);//assign a ticket
        token.append(" ").append(ticket);//0 embedded to token
        token.append("-").append(stub);//1
        token.append("-").append(start); //2
        token.append("-").append(index);//3 -- cluster name suffix
        token.append("-").append(hash); //4 -- hash
        return token.toString();
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
    //public  static OnSession validToken(MessageDigest messageDigest, String token) {
        //System.out.println(token);
        //ValidationUtil.Token validated = ValidationUtil.validToken(messageDigest,token);
        //if(!validated.valid) throw new RuntimeException("Wrong session token");
        //return new OnSessionTrack(1,validated.stub,validated.ticket,validated.index);//need to update
    //}
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

    public static ApplicationPreSetup applicationPreSetup(String className){
        try{
            return (ApplicationPreSetup)Class.forName(className).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static GameModule gameModule(String className){
        try{
            return (GameModule) Class.forName(className).getConstructor().newInstance();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static String toBase64String(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }
    public static byte[] fromBase64String(String data){
        return Base64.getDecoder().decode(data);
    }

    public static byte[] fromPemString(String base64Key){
        String privateKeyPEM = base64Key.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll("\n", "").replace("-----END PRIVATE KEY-----", "");
        return Base64.getDecoder().decode(privateKeyPEM);
    }

    public static String toString(Exception ex){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        ex.printStackTrace(pw);
        return sw.getBuffer().toString();
    }


}
