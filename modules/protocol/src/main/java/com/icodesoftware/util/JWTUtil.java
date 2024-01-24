package com.icodesoftware.util;

import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class JWTUtil {
    //Secret key algorithm
    private final static String ALG_HMAC = "HmacSHA256";

    //private public key pair algorithm RSA or ECDSA
    private final static String ALG_RSA = "SHA256WithRSA";
    //private final static String ALG_ECDSA = "ECDSA";
    private final static int KEY_SIZE = 32;
    private static SecureRandom secureRandom;

    static {
        secureRandom = new SecureRandom();
    }

    public static byte[] key(){
        byte[] key = new byte[KEY_SIZE];
        secureRandom.nextBytes(key);
        return key;
    }

    public static JWT init() {
        try {
            Mac mac = Mac.getInstance(ALG_HMAC);
            SecretKeySpec secretKey = new SecretKeySpec(key(), ALG_HMAC);
            mac.init(secretKey);
            return new JWT(mac);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static JWT init(byte[] key) {
        try {
            if (key.length < KEY_SIZE) throw new IllegalArgumentException("Key size should be more 32 bytes");
            Mac mac = Mac.getInstance(ALG_HMAC);
            SecretKeySpec secretKey = new SecretKeySpec(key, ALG_HMAC);
            mac.init(secretKey);
            return new JWT(mac);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static JWT init(PrivateKey privateKey) {
        try{
            Signature sig = Signature.getInstance(ALG_RSA);
            sig.initSign(privateKey);
            return new JWT(sig,true);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static JWT init(PublicKey publicKey) {
        try{
            Signature sig = Signature.getInstance(ALG_RSA);
            sig.initVerify(publicKey);
            return new JWT(sig,false);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static class JWT{
        private final Mac mac;
        private final Signature signer;
        private final Signature verifier;

        private final String alg;
        public JWT(Mac mac){
            this.mac = mac;
            this.signer = null ;
            this.verifier = null;
            this.alg = "HS256";
        }

        public JWT(Signature signature,boolean signing){
            if(signing){
                this.signer = signature;
                this.verifier = null;
            }
            else{
                this.signer = null;
                this.verifier = signature;
            }
            this.alg = "RS256";
            this.mac = null ;
        }

        public String token(OnProcess onProcess){
            try{
                JsonObject header = new JsonObject();
                header.addProperty("alg",alg);
                header.addProperty("typ","JWT");
                JsonObject payload = new JsonObject();
                if(!onProcess.process(header,payload)) throw new IllegalArgumentException("should be return true");
                StringBuffer buffer = new StringBuffer(base64(header));
                buffer.append(".").append(base64(payload));
                String signature = sign(buffer.toString().getBytes(),new byte[0]);
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
                String signature = sign(buffer.toString().getBytes(),parts[2].getBytes());
                JsonObject h = json(parts[0]);
                JsonObject p = json(parts[1]);
                if(!signature.equals(parts[2])) throw new RuntimeException("Bad signature");//need to verify json parser sync issue
                return onVerify.process(h,p);
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }

        private JsonObject json(String base64){
            return JsonUtil.parse(Base64.getUrlDecoder().decode(base64));
        }
        private String base64(JsonObject json){
            return Base64.getUrlEncoder().encodeToString(json.toString().getBytes());
        }

        private String sign(byte[] data,byte[] signature) throws Exception{
            if(mac != null){
                synchronized (mac) {
                    return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data));
                }
            }
            if(signer!= null) {
                synchronized (signer) {
                    signer.update(data);
                    return Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());
                }
            }
            if(verifier!=null){
                synchronized (verifier){
                    verifier.update(data);
                    byte[] original = Base64.getUrlDecoder().decode(signature);
                    if(verifier.verify(original)) return Base64.getUrlEncoder().withoutPadding().encodeToString(original);
                    return "bad signature";
                }
            }
            throw new IllegalArgumentException("no sign tool provided");
        }
        public interface OnProcess{
            boolean process(JsonObject header,JsonObject payload);
        }
    }
}
