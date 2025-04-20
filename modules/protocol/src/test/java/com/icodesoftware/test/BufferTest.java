package com.icodesoftware.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.Tournament;
import com.icodesoftware.util.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
    public void keyToBufferTest() {
        Recoverable.DataBuffer fk = BufferProxy.buffer(10, SnowflakeKey.from(10));
        Assert.assertEquals(fk.readLong(),10L);
        Recoverable.DataBuffer nk = BufferProxy.buffer(100, NaturalKey.from("tester"));
        Assert.assertEquals(nk.readUTF8(),"tester");
        Recoverable.DataBuffer ik = BufferProxy.buffer(100, IntegerKey.from(90));
        Assert.assertEquals(ik.readInt(),90);
    }

    @Test(groups = { "buffer" })
    public void bufferEndianTest() {
        Recoverable.DataBuffer fk = BufferProxy.buffer(10, SnowflakeKey.from(10));
        Recoverable.DataBuffer tk = BufferProxy.buffer(10,false);
        tk.src().order(ByteOrder.BIG_ENDIAN);
        tk.writeLong(fk.readLong());
        tk.flip();
        Assert.assertEquals(tk.readLong(),10L);
        //SnowflakeKey key = SnowflakeKey.from(10);
        //System.out.println(ByteOrder.nativeOrder());
        Assert.assertEquals(ByteBuffer.allocate(10).order(),ByteOrder.BIG_ENDIAN);
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
