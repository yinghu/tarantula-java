package com.icodesoftware.test;

import com.icodesoftware.protocol.DataBuffer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by yinghu lu on 9/28/2020.
 */
public class DataBufferTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "data buffer" })
    public void payloadBufferTest() {
        DataBuffer payloadBuffer = new DataBuffer();
        payloadBuffer.putUTF8("hello");
        payloadBuffer.putInt(15);
        payloadBuffer.putUTF8("pop");
        Assert.assertTrue(payloadBuffer.toArray().length==20);
        DataBuffer out = new DataBuffer(payloadBuffer.toArray());
        Assert.assertTrue(out.getUTF8().equals("hello"));
        Assert.assertTrue(out.getInt()==15);
        Assert.assertTrue(out.getUTF8().equals("pop"));
    }

}
