package com.icodesoftware.test;

import com.google.gson.JsonObject;
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
        payloadBuffer.putByteArray("12345".getBytes());
        DataBuffer out = new DataBuffer(payloadBuffer.toArray());
        Assert.assertTrue(out.getUTF8().equals("hello"));
        Assert.assertTrue(out.getInt()==15);
        Assert.assertTrue(out.getUTF8().equals("pop"));
        Assert.assertTrue(new String(out.getByteArray()).equals("12345"));
    }
    @Test(groups = { "data buffer" })
    public void payloadSizeTest(){
        JsonObject jsonObject = new JsonObject();
        DataBuffer buffer = new DataBuffer();
        jsonObject.addProperty("1",5);
        jsonObject.addProperty("2","test");
        buffer.putInt(5);
        buffer.putUTF8("test");
        Assert.assertTrue(buffer.toArray().length==12);
        Assert.assertTrue(jsonObject.toString().length()==18);
    }
}
