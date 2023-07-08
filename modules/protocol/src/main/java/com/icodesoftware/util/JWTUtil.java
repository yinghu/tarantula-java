package com.icodesoftware.util;

import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class JWTUtil {
    private final static String ALG = "HmacSHA256";
    private final static int KEY_SIZE = 32;
    private static SecureRandom secureRandom;
    static {
        secureRandom = new SecureRandom();
    }

    public static JWT init(){
        try{
            byte[] key = new byte[KEY_SIZE];
            secureRandom.nextBytes(key);
            Mac mac = Mac.getInstance(ALG);
            SecretKeySpec secretKey = new SecretKeySpec(key, ALG);
            mac.init(secretKey);
            return new JWT(mac);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static class JWT{
        private final Mac mac;
        public JWT(Mac mac){
            this.mac = mac;
        }

        public String token(OnProcess onProcess){
            try{
                JsonObject header = new JsonObject();
                header.addProperty("alg","HS256");
                header.addProperty("typ","JWT");
                JsonObject payload = new JsonObject();
                if(!onProcess.process(header,payload)) throw new IllegalArgumentException("should be return true");
                StringBuffer buffer = new StringBuffer(base64(header));
                buffer.append(".").append(base64(payload));
                String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(buffer.toString().getBytes()));
                return buffer.append(".").append(signature).toString();
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }

        public boolean verify(String token,OnProcess onVerify){
            try{
                String[] parts = token.split("\\.");
                StringBuffer buffer = new StringBuffer(parts[0]);
                buffer.append(".").append(parts[1]);
                String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(buffer.toString().getBytes()));
                JsonObject h = JsonUtil.parse(Base64.getDecoder().decode(parts[0]));
                JsonObject p = JsonUtil.parse(Base64.getDecoder().decode(parts[1]));
                return signature.equals(parts[2]) && onVerify.process(h,p);
            }catch (Exception ex){
                return false;
            }
        }

        private String base64(JsonObject json){
            return Base64.getEncoder().encodeToString(json.toString().getBytes());
        }

        public interface OnProcess{
            boolean process(JsonObject header,JsonObject payload);
        }
    }
}
