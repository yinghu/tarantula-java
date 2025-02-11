package com.icodesoftware.test;


import com.icodesoftware.OnSession;
import com.icodesoftware.protocol.CryptoManager;

import com.icodesoftware.protocol.session.AccessKeyTrack;
import com.icodesoftware.protocol.session.OnSessionTrack;
import com.icodesoftware.service.AccessKey;
import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.JWTUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CryptoManagerTest extends LoggerSetup{


    @Test(groups = { "CryptoManager" })
    public void hashTest() {
        CryptoManager.init();
        String hash1 = CryptoManager.hash("hello");
        String hash2 = CryptoManager.hash("hello");
        Assert.assertEquals(hash1,hash2);
        String hash3 = CryptoManager.hash("hellp");
        Assert.assertNotEquals(hash1,hash3);
        Assert.assertNotEquals(hash2,hash3);
    }

    @Test(groups = { "CryptoManager" })
    public void tokenTest() {
        CryptoManager.init();
        OnSession onSession = new OnSessionTrack(100,200);
        String token = CryptoManager.token(onSession);
        OnSession verify = CryptoManager.verify(token);
        Assert.assertEquals(onSession.systemId(),verify.systemId());
        Assert.assertEquals(onSession.stub(),verify.stub());
    }

    @Test(groups = { "CryptoManager" })
    public void accessKeyTest() {
        CryptoManager.init();
        AccessKeyTrack accessKeyTrack = new AccessKeyTrack();
        accessKeyTrack.distributionId(100);
        accessKeyTrack.name("test");
        Assert.assertEquals(accessKeyTrack.keyId(),100);
        Assert.assertEquals(accessKeyTrack.name(),"test");
        String accessKey = CryptoManager.accessKey(accessKeyTrack);
        AccessKey track = CryptoManager.validateAccessKey(accessKey);
        Assert.assertEquals(accessKeyTrack.keyId(),track.keyId());
        Assert.assertEquals(accessKeyTrack.name(),track.name());
        Assert.assertTrue(track.valid());
        Assert.assertTrue(accessKeyTrack.valid());
    }

    @Test(groups = { "CryptoManager" })
    public void ticketTest() {
        CryptoManager.init();
        String ticket = CryptoManager.ticket(100,200,1);
        Assert.assertTrue(CryptoManager.validateTicket(100,200,ticket));
        try{Thread.sleep(2000);}catch (Exception ex){}
        Assert.assertFalse(CryptoManager.validateTicket(100,200,ticket));

    }

    @Test(groups = { "CryptoManager" })
    public void hashTest1() {
        byte[] keyForJwt = JWTUtil.key();
        byte[] keyForCipher = CipherUtil.key();
        byte[] keyForSigner = JWTUtil.key();
        CryptoManager.init(keyForJwt,keyForCipher,keyForSigner);
        String hash1 = CryptoManager.hash("hello");
        String hash2 = CryptoManager.hash("hello");
        Assert.assertEquals(hash1,hash2);
        String hash3 = CryptoManager.hash("hellp");
        Assert.assertNotEquals(hash1,hash3);
        Assert.assertNotEquals(hash2,hash3);
    }

    @Test(groups = { "CryptoManager" })
    public void tokenTest1() {
        byte[] keyForJwt = JWTUtil.key();
        byte[] keyForCipher = CipherUtil.key();
        byte[] keyForSigner = JWTUtil.key();
        CryptoManager.init(keyForJwt,keyForCipher,keyForSigner);
        OnSession onSession = new OnSessionTrack(100,200);
        String token = CryptoManager.token(onSession);
        OnSession verify = CryptoManager.verify(token);
        Assert.assertEquals(onSession.systemId(),verify.systemId());
        Assert.assertEquals(onSession.stub(),verify.stub());
    }

    @Test(groups = { "CryptoManager" })
    public void accessKeyTest1() {
        byte[] keyForJwt = JWTUtil.key();
        byte[] keyForCipher = CipherUtil.key();
        byte[] keyForSigner = JWTUtil.key();
        CryptoManager.init(keyForJwt,keyForCipher,keyForSigner);
        AccessKeyTrack accessKeyTrack = new AccessKeyTrack();
        accessKeyTrack.distributionId(100);
        accessKeyTrack.name("test");
        Assert.assertEquals(accessKeyTrack.keyId(),100);
        Assert.assertEquals(accessKeyTrack.name(),"test");
        String accessKey = CryptoManager.accessKey(accessKeyTrack);
        AccessKey track = CryptoManager.validateAccessKey(accessKey);
        Assert.assertEquals(accessKeyTrack.keyId(),track.keyId());
        Assert.assertEquals(accessKeyTrack.name(),track.name());
        Assert.assertTrue(track.valid());
        Assert.assertTrue(accessKeyTrack.valid());
    }

    @Test(groups = { "CryptoManager" })
    public void ticketTest1() {
        byte[] keyForJwt = JWTUtil.key();
        byte[] keyForCipher = CipherUtil.key();
        byte[] keyForSigner = JWTUtil.key();
        CryptoManager.init(keyForJwt,keyForCipher,keyForSigner);
        String ticket = CryptoManager.ticket(100,200,1);
        Assert.assertTrue(CryptoManager.validateTicket(100,200,ticket));
        try{Thread.sleep(2000);}catch (Exception ex){}
        Assert.assertFalse(CryptoManager.validateTicket(100,200,ticket));

    }
}
