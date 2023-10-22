package com.tarantula.test;

import com.tarantula.platform.AssociateKey;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;

public class AssociateKeyTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "AssociateKey" })
    public void associateKeyTest() {
        AssociateKey key = new AssociateKey(100,"TEST");
        byte[] b100 = ByteBuffer.allocate(16).order(ByteOrder.nativeOrder()).putLong(100).putInt(4).put("TEST".getBytes()).flip().array();
        Assert.assertEquals(key.asBinary(),b100);
        Assert.assertEquals(key.asString(), Base64.getEncoder().encodeToString(b100));
    }

}
