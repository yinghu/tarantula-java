package com.tarantula.test;

import com.icodesoftware.Access;
import com.icodesoftware.OnSession;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.JWTUtil;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.AccessControl;
import com.tarantula.platform.OnSessionTrack;
import com.tarantula.platform.presence.User;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.HashMap;

public class JWTTokenTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = {"JWT"})
    public void jwtTokenTest(){
        byte[] key = CipherUtil.key(16);
        JWTUtil.JWT jwt = JWTUtil.init(CipherUtil.key(32));
        Access access = new User();
        access.id(1000);
        access.role(AccessControl.root.name());
        OnSession session = new OnSessionTrack();
        session.stub(1);
        HashMap<String, Access.Role> roles = new HashMap<>();
        roles.put(AccessControl.root.name(),AccessControl.root);
        String jwtToken = jwt.token((h, p) -> {
            Access.Role r = roles.get(access.role());
            byte[] mark = waterMark(key, ByteBuffer.allocate(16).putLong(access.id()).putInt(session.stub()).putInt(r.accessControl()).array());
            h.addProperty("kid", CipherUtil.toBase64Key(mark));
            p.addProperty("aud", access.role());
            long expiry = TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusHours(24));
            p.addProperty("exp", expiry);
            return true;
        });
        Assert.assertTrue(jwt.verify(jwtToken, (h, p) -> {
            if(TimeUtil.expired(TimeUtil.fromUTCMilliseconds(p.get("exp").getAsLong()))) return false;
            Access.Role r = roles.get(p.get("aud").getAsString());
            if(r==null) return false;
            byte[] data = reverse(key, CipherUtil.fromBase64Key(h.get("kid").getAsString()));
            ByteBuffer buffer = ByteBuffer.allocate(16).put(data).flip();
            long id = buffer.getLong();
            int stub = buffer.getInt();
            int role = buffer.getInt();
            //System.out.println(id+"<><>"+stub);
            return id == access.id() && stub == session.stub() && role == r.accessControl();
        }));
    }

    private byte[] reverse(byte[] key, byte[] data) {
        try {
            return CipherUtil.decrypt(key).doFinal(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    private byte[] waterMark(byte[] key, byte[] data) {
        try {
            return CipherUtil.encrypt(key).doFinal(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
