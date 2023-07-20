package com.tarantula.platform.configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class AWSSigner {

    private final static String ALG_HMAC = "HmacSHA1";
    private Mac mac;

    public void init(String key) throws Exception{
        mac = Mac.getInstance(ALG_HMAC);
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALG_HMAC);
        mac.init(secretKey);
    }
    public static String signingDate(){
        return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").format(ZonedDateTime.now(ZoneOffset.UTC));
    }
    public String sign(String httpMethod,String date,String url) throws Exception{
        StringBuffer data = new StringBuffer(httpMethod).append("\n\n\n").append(date).append("\n").append(url);
        synchronized (mac){
            return Base64.getEncoder().encodeToString(mac.doFinal(data.toString().getBytes()));
        }
    }

}
