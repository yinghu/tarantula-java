package com.icodesoftware.lmdb.test;

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
    public void bufferUnSafeTest() throws Exception{
        ByteBuffer buffer = ByteBuffer.allocateDirect(700);
        Unsafe unsafe = UnsafeUtil.useUnsafe();
        BufferProxy proxy = (BufferProxy) unsafe.allocateInstance(BufferProxy.class);
        proxy.buffer = buffer;
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

}
