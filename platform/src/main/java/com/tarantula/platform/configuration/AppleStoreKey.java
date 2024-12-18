package com.tarantula.platform.configuration;

import com.google.gson.JsonObject;
import com.icodesoftware.TarantulaLogger;
import com.icodesoftware.logging.JDKLogger;
import com.icodesoftware.service.Content;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.JWTUtil;
import com.tarantula.platform.util.SystemUtil;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;

public class AppleStoreKey implements VendorValidator{

    private TarantulaLogger logger = JDKLogger.getLogger(AppleStoreKey.class);

    public static String production_transaction_url = "https://api.storekit.itunes.apple.com/inApps/v1/transactions/";

    public static String sandbox_transaction_url = "https://api.storekit-sandbox.itunes.apple.com/inApps/v1/transactions/";

    private final JsonObject header;
    private final Content content;
    private JWTUtil.JWT jwt;
    public AppleStoreKey(JsonObject payload, Content key){
        this.header = payload;
        this.content = key;
    }

    public boolean isSandbox(){
        return true;
    }

    public String keyId(){
        return header.get("KeyId").getAsString();
    }
    public String issuerId(){
        return header.get("IssuerId").getAsString();
    }
    public String bundleId(){
        return header.get("BundleId").getAsString();
    }
    public String secureKey(){
        return header.get("SecretKey").getAsString();
    }

    public String token(){
        return jwt.token((h,p)->{
            h.addProperty("kid",keyId());
            p.addProperty("aud","appstoreconnect-v1");
            long x = Instant.now().getEpochSecond();
            p.addProperty("iat",x);
            long y = x+3600;
            p.addProperty("exp",y);
            p.addProperty("iss",issuerId());
            p.addProperty("bid",bundleId());
            p.addProperty("scope","[GET /v1/apps?filter[platform]=IOS]");
            return true;
        });
    }

    @Override
    public boolean validate(ServiceContext serviceContext) {
        try{
            byte[] key = SystemUtil.fromPemString(new String(content.data()));
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);//"secp256r1"
            ECPrivateKey privateKey  = (ECPrivateKey)keyFactory.generatePrivate(keySpec);
            jwt = JWTUtil.initWithES256(privateKey);
            logger.warn(token());
            return true;
        }catch (Exception ex){
            logger.error("Error on load key",ex);
            return false;
        }
    }
}
