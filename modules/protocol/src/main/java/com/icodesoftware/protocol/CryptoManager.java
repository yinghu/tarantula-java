package com.icodesoftware.protocol;

import com.icodesoftware.OnSession;
import com.icodesoftware.Recoverable;
import com.icodesoftware.protocol.service.TRAccessKey;
import com.icodesoftware.protocol.presence.TROnSession;
import com.icodesoftware.service.AccessKey;
import com.icodesoftware.service.TokenValidatorProvider;
import com.icodesoftware.util.*;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HexFormat;

public class CryptoManager {
    
    private static boolean started;

    private static MessageDigest messageDigest;
    private static JWTUtil.JWT jwt;
    private static Cipher encrypt;
    private static Cipher decrypt;
    private static Mac mac;
    private static int tokenDurationHours = 20;

    private CryptoManager(){}

    public static void init(byte[] keyForJwt,byte[] keyForCipher,byte[] keyForSigner){
        if(started) return;
        try{
            jwt = JWTUtil.init(keyForJwt);
            messageDigest = MessageDigest.getInstance(TokenValidatorProvider.MDA);
            encrypt = CipherUtil.encrypt(keyForCipher);
            decrypt = CipherUtil.decrypt(keyForCipher);
            mac = Mac.getInstance(JWTUtil.ALG_HMAC);
            SecretKeySpec secretKey = new SecretKeySpec(keyForSigner, JWTUtil.ALG_HMAC);
            mac.init(secretKey);
            started = true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static void init(){
        if(started) return;
        try{
            jwt = JWTUtil.init();
            messageDigest = MessageDigest.getInstance(TokenValidatorProvider.MDA);
            byte[] key = CipherUtil.key();
            encrypt = CipherUtil.encrypt(key);
            decrypt = CipherUtil.decrypt(key);
            mac = Mac.getInstance(JWTUtil.ALG_HMAC);
            SecretKeySpec secretKey = new SecretKeySpec(JWTUtil.key(), JWTUtil.ALG_HMAC);
            mac.init(secretKey);
            started = true;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static String hash(String text){
        try{
            MessageDigest clone = (MessageDigest) messageDigest.clone();
            clone.reset();
            clone.update(text.getBytes());
            return HexFormat.of().formatHex(clone.digest());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private static byte[] encrypt(byte[] data){
        try{
            synchronized (encrypt){
                return encrypt.doFinal(data);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    private static byte[] decrypt(byte[] data){
        try{
            synchronized (decrypt){
                return decrypt.doFinal(data);
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    //jwt token with longer hours duration
    public static String token(OnSession session){
        return jwt.token((h,p)->{
            long expiry = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusHours(tokenDurationHours));
            Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(16,false);
            dataBuffer.writeLong(session.systemId()).writeLong(session.stub());
            byte[] mark = encrypt(dataBuffer.array());
            h.addProperty("kid",CipherUtil.toBase64Key(mark));
            p.addProperty("aud",session.role());
            p.addProperty("exp",expiry);
            return true;
        });
    }

    public static OnSession verify(String token){
        TROnSession onSession = new TROnSession();
        if(!jwt.verify(token,(h,p)->{
            long expiry = p.get("exp").getAsLong();
            if (TimeUtil.expired(TimeUtil.fromUTCMilliseconds(expiry))) {
                return false;
            }
            String role = p.get("aud").getAsString();
            byte[] data = decrypt(CipherUtil.fromBase64Key(h.get("kid").getAsString()));
            Recoverable.DataBuffer dataBuffer = BufferProxy.wrap(data);
            long id = dataBuffer.readLong();
            long stub = dataBuffer.readLong();
            onSession.distributionId(id);
            onSession.stub(stub);
            onSession.role(role);
            return true;
        })) return TROnSession.INVALID_TOKEN;
        return onSession;
    }

    //server access token than can be revoked
    public static String accessKey(AccessKey accessKey){
        byte[] name = accessKey.name().getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(name.length+8);
        buffer.putLong(accessKey.keyId());
        buffer.put(name);
        byte[] data = buffer.array();
        StringBuffer key = new StringBuffer(Base64Util.toBase64String(encrypt(data)));
        key.append(".");
        synchronized (mac){
            byte[] signature = mac.doFinal(data);
            key.append(Base64Util.toBase64String(signature));
        }
        return key.toString();
    }

    public static AccessKey validateAccessKey(String key){
        String[] parts = key.split("\\.");
        byte[] data = decrypt(Base64Util.fromBase64String(parts[0]));
        byte[] signature = Base64Util.fromBase64String(parts[1]);
        boolean validated;
        synchronized (mac){
            validated = Arrays.equals(mac.doFinal(data),signature);
        }
        if(!validated) throw new RuntimeException("Invalid access key");
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte[] name = new byte[buffer.remaining()-8];
        long keyId = buffer.getLong();
        buffer.get(name);
        TRAccessKey accessKeyTrack = new TRAccessKey();
        accessKeyTrack.distributionId(keyId);
        accessKeyTrack.name(new String(name));
        return accessKeyTrack;
    }

    //short live ticket / usually 3-5 seconds
    public static String ticket(long systemId,long stub,int duration){
        long expired = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusSeconds(duration));
        byte[] data = BufferProxy.buffer(24,false).writeLong(expired).writeLong(systemId).writeLong(stub).array();
        byte[] mark = encrypt(data);
        return Base64Util.toBase64String(mark);
    }

    public static boolean validateTicket(long key,long stub,String ticket){
        byte[] mark = decrypt(Base64Util.fromBase64String(ticket));
        Recoverable.DataBuffer buffer = BufferProxy.wrap(mark);
        if(TimeUtil.expired(TimeUtil.fromUTCMilliseconds(buffer.readLong()))) return false;
        return buffer.readLong()==(key) && buffer.readLong() == stub;
    }


}
