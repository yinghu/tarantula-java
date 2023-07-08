package com.icodesoftware.test;

import com.icodesoftware.util.JWTUtil;
import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
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

}
