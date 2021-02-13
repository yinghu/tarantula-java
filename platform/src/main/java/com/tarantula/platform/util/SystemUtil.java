package com.tarantula.platform.util;

import com.icodesoftware.OnSession;
import com.icodesoftware.Recoverable;
import com.tarantula.platform.OnSessionTrack;

import java.security.MessageDigest;
import java.time.*;
import java.util.Arrays;

import java.util.UUID;

/**
 * updated by yinghu lu on 1/27/21.
 */
public class SystemUtil {

    public static long toUTCMilliseconds(LocalDateTime dateTime){
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    public static LocalDateTime fromUTCMilliseconds(long milliseconds){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), ZoneOffset.UTC);
    }
    public static long durationUTCMilliseconds(LocalDateTime start,LocalDateTime end){
        return Duration.between(start,end).toMillis();
    }
    public static long durationUTCInSeconds(LocalDateTime start,LocalDateTime end){
        return Duration.between(start,end).toMillis()/1000;
    }
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
    public static String ticket(MessageDigest messageDigest, String systemId, int stub, int durationSeconds) {
        LocalDateTime _st = LocalDateTime.now().plusSeconds(durationSeconds);
        long end = SystemUtil.toUTCMilliseconds(_st);
        StringBuffer _ticket = new StringBuffer();
        _ticket.append("tarantula").append(" ").append(end).append(" ");
        messageDigest.reset();
        messageDigest.update(systemId.getBytes());
        messageDigest.update(Integer.toHexString(stub).getBytes());
        messageDigest.update(Long.toHexString(end).getBytes());
        String hash = SystemUtil.toHexString(messageDigest.digest());
        _ticket.append(hash);
        return _ticket.toString();
    }

    public static boolean validTicket(MessageDigest messageDigest,String systemId,int stub,String ticket){
        String[] tlist = ticket.split(" ");//validate
        long end = Long.parseLong(tlist[1]);
        messageDigest.reset();
        messageDigest.update(systemId.getBytes());
        messageDigest.update(Integer.toHexString(stub).getBytes());
        messageDigest.update(Long.toHexString(end).getBytes());
        if(tlist[2].equals(SystemUtil.toHexString(messageDigest.digest()))){
            LocalDateTime ending = SystemUtil.fromUTCMilliseconds(end);
            return ending.isAfter(LocalDateTime.now());
        }
        else{
            return false;
        }
    }
    public static  String token(MessageDigest messageDigest, String systemId,int stub,int timeoutMinutes) {
        //{systemId} {ticket}-{routing}-{stub}-{cid}-{start}-{hash}
        //ticket=> {tarantula} {stub} {end} {hash}
        StringBuffer token = new StringBuffer(systemId);
        messageDigest.reset();
        messageDigest.update(systemId.getBytes());
        messageDigest.update(Integer.toHexString(stub).getBytes());
        long start = SystemUtil.toUTCMilliseconds(LocalDateTime.now());
        messageDigest.update(Long.toHexString(start).getBytes());
        String hash = SystemUtil.toHexString(messageDigest.digest());
        String ticket = SystemUtil.ticket(messageDigest,systemId,stub,timeoutMinutes*60);//assign a ticket
        token.append(" ").append(ticket);//0 embedded to token
        token.append("-").append(stub);//1
        token.append("-").append(start); //2
        token.append("-").append(hash); //3
        return token.toString();
    }
    public static  String accessKey(MessageDigest messageDigest,String typeId,String gameClusterId,long timestamp) {
        //{gameClusterId}-{hash}
        StringBuffer token = new StringBuffer(gameClusterId);
        messageDigest.reset();
        messageDigest.update(typeId.getBytes());
        messageDigest.update(gameClusterId.getBytes());
        messageDigest.update(Long.toHexString(timestamp).getBytes());//saved on game cluster id
        String hash = SystemUtil.toHexString(messageDigest.digest());
        token.append("-").append(hash);
        return token.toString();
    }
    public static boolean validAccessKey(MessageDigest messageDigest,String accessKey,String typeId,long timestamp){
        String[] sp = accessKey.split("-");
        messageDigest.reset();
        messageDigest.update(typeId.getBytes());
        messageDigest.update(sp[0].getBytes());
        messageDigest.update(Long.toHexString(timestamp).getBytes());
        String hash = SystemUtil.toHexString(messageDigest.digest());
        return hash.equals(sp[1]);
    }
    public  static OnSession validToken(MessageDigest messageDigest, String token) {
        //System.out.println(token);
        int sp = token.indexOf(" ");
        String systemId = token.substring(0,sp);
        String[] vm = token.substring(sp+1).split("-");
        //vm[0] - ticket vm[1] - stub vm[2] - start vm[3] --hash
        messageDigest.reset();
        messageDigest.update(systemId.getBytes());//systemId
        messageDigest.update(Integer.toHexString(Integer.parseInt(vm[1])).getBytes());//stub
        messageDigest.update(Long.toHexString(Long.parseLong(vm[2])).getBytes());//start
        if(SystemUtil.toHexString(messageDigest.digest()).equals(vm[3])){// hash
            return new OnSessionTrack(systemId,Integer.parseInt(vm[1]),vm[0]);
        }
        else{
            throw new RuntimeException("Wrong session token");
        }
    }
    public static String hashPassword(MessageDigest messageDigest,String password) {
        messageDigest.reset();
        messageDigest.update(password.getBytes());
        return SystemUtil.toHexString(messageDigest.digest());
    }

    public static int partition(byte[] key,int partitionNumber){
        return Math.abs(Arrays.hashCode(key))%partitionNumber;
    }
    public static int partition(String key,int partitionNumber){
        return Math.abs(Arrays.hashCode(key.getBytes()))%partitionNumber;
    }
    public static String toString(byte[] data){
        StringBuffer sb = new StringBuffer(data.length);
        for(byte b:data){
            sb.append((char)b);
        }
        return sb.toString();
    }
    public static String toString(String[] list){
        StringBuffer buffer = new StringBuffer();
        for(String s : list){
            buffer.append(s).append(Recoverable.PATH_SEPARATOR);
        }
        return buffer.substring(0,buffer.length()-1);
    }

    public static long toMidnight(){
        LocalTime mid = LocalTime.MIDNIGHT;
        LocalDate date = LocalDate.now();
        LocalDateTime end = LocalDateTime.of(date.plusDays(1),mid);
        return Duration.between(LocalDateTime.now(),end).toMillis();
    }
    public static String toCreditsString(double vc){
        if(vc>=1000000000){ // B level
            return String.format("%.2f%c",Double.valueOf(vc/1000000000).doubleValue(),'B');
        }
        else if(vc>=1000000&&vc<1000000000){ //1M to 999M
            return String.format("%.2f%c",Double.valueOf(vc/1000000).doubleValue(),'M');
        }
        else if(vc>=1000&&vc<1000000){ //1K to 999K
            return String.format("%.2f%c",Double.valueOf(vc/1000).doubleValue(),'K');
        }
        else{
            return String.format("%.2f",Double.valueOf(vc).doubleValue());
        }
    }
    public static String toCreditsString2(double vc){
        if(vc>=1000000000){ // B level
            return String.format("%.0f%c",Double.valueOf(vc/1000000000).doubleValue(),'B');
        }
        else if(vc>=1000000&&vc<1000000000){ //1M to 999M
            return String.format("%.0f%c",Double.valueOf(vc/1000000).doubleValue(),'M');
        }
        else if(vc>=1000&&vc<1000000){ //1K to 999K
            return String.format("%.0f%c",Double.valueOf(vc/1000).doubleValue(),'K');
        }
        else{
            return String.format("%.0f",Double.valueOf(vc).doubleValue());
        }
    }
    public static String oid(){
        return UUID.randomUUID().toString().replace("-","");
    }
    public static boolean timeout(long end){
        long dm = durationUTCInSeconds(LocalDateTime.now(),fromUTCMilliseconds(end));
        return dm<=0;
    }
    public static String remainingTimeAsString(long end,int size){
        long dm = durationUTCInSeconds(LocalDateTime.now(),fromUTCMilliseconds(end));
        if(dm<=0){
            return "00:00:00";
        }
        StringBuffer sb = new StringBuffer();
        if(size==3){
            long h = dm/(3600);
            sb.append(h>9?h:"0"+h).append(":");
            dm = dm%(3600);
            long m = dm/(60);
            sb.append(m>9?m:"0"+m).append(":");
            dm= dm%(60);
            sb.append(dm>9?dm:"0"+dm);
        }
        else if(size==2){
            //long h = dm/(3600);
            //sb.append(h>9?h:"0"+h).append(":");
            dm = dm%(3600);
            long m = dm/(60);
            sb.append(m>9?m:"0"+m).append(":");
            dm= dm%(60);
            sb.append(dm>9?dm:"0"+dm);
        }
        else{
            //long h = dm/(3600);
            //sb.append(h>9?h:"0"+h).append(":");
            //dm = dm%(3600);
            //long m = dm/(60);
            //sb.append(m>9?m:"0"+m).append(":");
            dm= dm%(60);
            sb.append(dm>9?dm:"0"+dm);
        }
        return sb.toString();
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
        return contentType;
    }
}
