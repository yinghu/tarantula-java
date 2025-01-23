package com.icodesoftware.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.DataBufferOutputStream;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BufferTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "buffer" })
    public void fullTest() {
        Recoverable.DataBuffer buffer = BufferProxy.buffer(4,true);
        Assert.assertFalse(buffer.full());
        buffer.writeInt(19);
        Assert.assertTrue(buffer.full());
    }

    @Test(groups = { "buffer" })
    public void transferTest() {
        DataBufferOutputStream dataBufferOutputStream = new DataBufferOutputStream(4,true);
        Exception exception = null;
        byte[] src = "long string value".getBytes();
        for(byte b : src){
            try{
                dataBufferOutputStream.write(b);
            }catch (Exception ex){
                exception = ex;
            }
        }
        Assert.assertNull(exception);
        exception = null;
        try{
            dataBufferOutputStream.flush();
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
        Assert.assertFalse(dataBufferOutputStream.src().full());
        Assert.assertEquals(src,dataBufferOutputStream.src().array());
    }
}
