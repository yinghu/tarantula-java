package com.icodesoftware.test;

import com.icodesoftware.protocol.PayloadBuffer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by yinghu lu on 9/28/2020.
 */
public class PendingInboundMessageTest{

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "payload buffer" })
    public void payloadBufferTest() {
        PayloadBuffer payloadBuffer = new PayloadBuffer();
        payloadBuffer.putUTF8("hello");
        payloadBuffer.putInt(15);
        payloadBuffer.putUTF8("pop");
        Assert.assertTrue(payloadBuffer.toArray().length==20);
        PayloadBuffer out = new PayloadBuffer(payloadBuffer.toArray());
        Assert.assertTrue(out.getUTF8().equals("hello"));
        Assert.assertTrue(out.getInt()==15);
        Assert.assertTrue(out.getUTF8().equals("pop"));
    }

}
