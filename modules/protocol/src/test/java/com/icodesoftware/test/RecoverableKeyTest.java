package com.icodesoftware.test;


import com.icodesoftware.util.BinaryKey;
import com.icodesoftware.util.NaturalKey;
import com.icodesoftware.util.SnowflakeKey;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

public class RecoverableKeyTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "RecoverableKey" })
    public void natureKeyTest() {
        NaturalKey key = new NaturalKey("TEST");
        byte[] b100 = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putInt(4).put("TEST".getBytes()).flip().array();
        Assert.assertEquals(key.asBinary(),b100);
        Assert.assertEquals(key.asString(), Base64.getEncoder().encodeToString(b100));
    }

    @Test(groups = { "RecoverableKey" })
    public void snowflakeKeyTest() {
        SnowflakeKey key = new SnowflakeKey(100);
        byte[] b100 = ByteBuffer.allocate(8).order(ByteOrder.nativeOrder()).putLong(100).flip().array();
        Assert.assertEquals(key.asBinary(),b100);
        Assert.assertEquals(key.asString(), Base64.getEncoder().encodeToString(b100));
    }

    @Test(groups = { "RecoverableKey" })
    public void binaryKeyTest() {
        BinaryKey key = new BinaryKey("test".getBytes());
        Assert.assertEquals(key.asBinary(),"test".getBytes());
        Assert.assertEquals(key.asString(), Base64.getEncoder().encodeToString("test".getBytes()));
    }
}
