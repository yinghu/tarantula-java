package com.icodesoftware.test;

import com.icodesoftware.util.JWTUtil;
import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.LocalDateTime;
public class JWTTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "JWT" })
    public void simpleTest() {
        JWTUtil.JWT jwt = JWTUtil.init();
        String jwtToken = jwt.token((header,payload)->{
            header.addProperty("kid","KID");
            payload.addProperty("sub", "BDS/");
            payload.addProperty("aud", "admin");
            long expiry = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusHours(1));
            payload.addProperty("exp",expiry);
            return true;
        });
        Assert.assertTrue(jwt.verify(jwtToken,(h,p)->
            p.get("aud").getAsString().equals("admin") && !TimeUtil.expired(TimeUtil.fromUTCMilliseconds(p.get("exp").getAsLong()))
        ));
    }
    @Test(groups = { "JWT" })
    public void simple2Test() {
        byte[] key = "AIzaSyB52WeIzCm0F6UUJ7XkYNDoTlx7Xeu8DMA".getBytes();
        JWTUtil.JWT jwt = JWTUtil.init(key);
        String jwtToken = jwt.token((header,payload)->{
            header.addProperty("kid","KID");
            payload.addProperty("sub", "BDS/");
            payload.addProperty("aud", "admin");
            long expiry = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusHours(1));
            payload.addProperty("exp",expiry);
            return true;
        });
        Assert.assertTrue(jwt.verify(jwtToken,(h,p)->
                p.get("aud").getAsString().equals("admin") && !TimeUtil.expired(TimeUtil.fromUTCMilliseconds(p.get("exp").getAsLong()))
        ));
    }
    @Test(groups = { "JWT" })
    public void simple3Test() throws Exception{
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair keyPair = kpg.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        JWTUtil.JWT jwt = JWTUtil.init(privateKey);
        String jwtToken = jwt.token((header,payload)->{
            header.addProperty("kid","KID");
            payload.addProperty("sub", "BDS/");
            payload.addProperty("aud", "admin");
            long expiry = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusHours(1));
            payload.addProperty("exp",expiry);
            return true;
        });
        JWTUtil.JWT pub = JWTUtil.init(keyPair.getPublic());
        Assert.assertTrue(pub.verify(jwtToken,(h,p)->
                p.get("aud").getAsString().equals("admin") && !TimeUtil.expired(TimeUtil.fromUTCMilliseconds(p.get("exp").getAsLong()))
        ));
    }
}
