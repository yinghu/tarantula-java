package com.icodesoftware.protocol;

import com.icodesoftware.util.TimeUtil;

import java.security.MessageDigest;
import java.time.LocalDateTime;

public class ValidationUtil {


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

    public static String validTicket(MessageDigest messageDigest,String systemId,int stub,String ticket){

        String[] tlist = ticket.split(" ");//validate
        long end = Long.parseLong(tlist[1]);
        messageDigest.reset();
        messageDigest.update(systemId.getBytes());
        messageDigest.update(Integer.toHexString(stub).getBytes());
        messageDigest.update(Long.toHexString(end).getBytes());
        if(tlist[2].equals(toHexString(messageDigest.digest()))){
            LocalDateTime ending = TimeUtil.fromUTCMilliseconds(end);
            return ending.isAfter(LocalDateTime.now())?tlist[3]:null;
        }
        else{
            return null;
        }
    }

    public  static Token validToken(MessageDigest messageDigest, String token) {
        int sp = token.indexOf(" ");
        String systemId = token.substring(0,sp);
        String[] vm = token.substring(sp+1).split("-");
        //vm[0] - ticket vm[1] - stub vm[2] - start vm[3] --hash  vm[4] -- cluster suffix
        messageDigest.reset();
        messageDigest.update(systemId.getBytes());//systemId
        messageDigest.update(Integer.toHexString(Integer.parseInt(vm[1])).getBytes());//stub
        messageDigest.update(Long.toHexString(Long.parseLong(vm[2])).getBytes());//start
        messageDigest.update(vm[3].getBytes());
        if(toHexString(messageDigest.digest()).equals(vm[4])){// hash
            return new Token(true,systemId,Integer.parseInt(vm[1]),vm[0],vm[3]);
        }
        else{
            return new Token(false);
        }
    }
    public static class Token{

        public boolean valid;
        public String systemId;
        public int stub;
        public String index;
        public String ticket;

        public Token(boolean valid){
            this.valid = valid;
        }

        public Token(boolean valid,String systemId,int stub,String index,String ticket){
            this.valid = valid;
            this.systemId = systemId;
            this.stub = stub;
            this.index = index;
            this.ticket = ticket;
        }
    }
}
