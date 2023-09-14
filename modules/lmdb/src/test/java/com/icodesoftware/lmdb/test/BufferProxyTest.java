package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.lmdb.BufferProxy;
import com.icodesoftware.util.UnsafeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;

public class BufferProxyTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "bufferProxy" })
    public void bufferTest() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(700);
        BufferProxy proxy = new BufferProxy(buffer);
        proxy.writeBoolean(true);
        proxy.writeDouble(10);
        proxy.writeShort((short)4);
        proxy.writeInt(100);
        proxy.writeFloat(99f);
        proxy.writeLong(800l);
        proxy.writeUTF8("hello");
        buffer.flip();
        Assert.assertTrue(proxy.readBoolean());
        Assert.assertEquals(proxy.readDouble(),10.0d);
        Assert.assertEquals(proxy.readShort(),4);
        Assert.assertEquals(proxy.readInt(),100);
        Assert.assertEquals(proxy.readFloat(),99f);
        Assert.assertEquals(proxy.readLong(),800l);
        Assert.assertEquals(proxy.readUTF8(),"hello");

        buffer.rewind();

        Assert.assertTrue(proxy.readBoolean());
        Assert.assertEquals(proxy.readDouble(),10.0d);
        Assert.assertEquals(proxy.readShort(),4);
        Assert.assertEquals(proxy.readInt(),100);
        Assert.assertEquals(proxy.readFloat(),99f);
        Assert.assertEquals(proxy.readLong(),800l);
        Assert.assertEquals(proxy.readUTF8(),"hello");
    }
    @Test(groups = { "bufferProxy" })
    public void bufferToArrayTest() {
        Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(200);
        dataBuffer.writeUTF8("test").writeInt(100).writeUTF8("loop");
        byte[] data = dataBuffer.array();
        Recoverable.DataBuffer dataWrap = BufferProxy.wrap(data);
        Assert.assertEquals(dataWrap.readUTF8(),"test");
        Assert.assertEquals(dataWrap.readInt(),100);
        Assert.assertEquals(dataWrap.readUTF8(),"loop");

        ByteBuffer direct = ByteBuffer.allocateDirect(100);
        Recoverable.DataBuffer buffer = new BufferProxy(direct);
        buffer.writeUTF8("test1000");
        direct.flip();
        byte[] ret = buffer.array();
        Assert.assertNotNull(ret);
    }

    @Test(groups = { "bufferProxy" })
    public void bufferUTFNullTest() {
        String n = null;
        Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(200);
        dataBuffer.writeUTF8(n).writeInt(100).writeUTF8("loop");
        byte[] data = dataBuffer.array();
        Recoverable.DataBuffer dataWrap = BufferProxy.wrap(data);
        Assert.assertEquals(dataWrap.readUTF8(),null);
        Assert.assertEquals(dataWrap.readInt(),100);
        Assert.assertEquals(dataWrap.readUTF8(),"loop");
    }


}
