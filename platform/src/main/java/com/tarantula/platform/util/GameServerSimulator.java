package com.tarantula.platform.util;

import com.tarantula.Session;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Created by yinghu lu on 9/19/2020.
 */
public class GameServerSimulator {
    static HttpCaller caller;
    public static void main(String[] args) throws Exception{
        caller = new HttpCaller("http://10.0.0.234:8090");
        caller._init();
        caller.index();
        System.out.println(OnTicket("BDS01/81280cec10d244d5a324d5fcb211fdcd-75596F4EB936FFF376D31E26D5F204F48E23C921"));
        System.out.println(key());
    }
    static String OnTicket(String accessKey) throws Exception{
        String[] headers = new String[]{
                Session.TARANTULA_ACCESS_KEY,accessKey,
                Session.TARANTULA_ACTION,"onTicket",
                Session.TARANTULA_SERVER_ID,"SERVER_ID"
        };
        return caller.get("server",headers);
    }
    static String key(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[16];
        secureRandom.nextBytes(key);
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
