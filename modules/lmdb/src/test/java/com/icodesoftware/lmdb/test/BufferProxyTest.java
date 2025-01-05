package com.icodesoftware.lmdb.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;

import com.icodesoftware.util.LocalHeader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BufferProxyTest {


    @Test(groups = { "bufferProxy" })
    public void bufferBasicTest() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(700);
        Recoverable.DataBuffer proxy = BufferProxy.buffer(buffer);
        Recoverable.DataHeader dataHeader = new LocalHeader(100,10,1);
        proxy.writeHeader(dataHeader);
        proxy.writeBoolean(true);
        proxy.writeByte((byte)1);
        proxy.writeDouble(10);
        proxy.writeShort((short)4);
        proxy.writeInt(100);
        proxy.writeFloat(99f);
        proxy.writeLong(800l);
        proxy.writeUTF8("hello");
        buffer.flip();
        Recoverable.DataHeader header1 = proxy.readHeader();
        Assert.assertEquals(header1.revision(),100);
        Assert.assertEquals(header1.factoryId(),10);
        Assert.assertEquals(header1.classId(),1);
        Assert.assertTrue(proxy.readBoolean());
        Assert.assertEquals(proxy.readByte(),(byte)1);
        Assert.assertEquals(proxy.readDouble(),10.0d);
        Assert.assertEquals(proxy.readShort(),4);
        Assert.assertEquals(proxy.readInt(),100);
        Assert.assertEquals(proxy.readFloat(),99f);
        Assert.assertEquals(proxy.readLong(),800l);
        Assert.assertEquals(proxy.readUTF8(),"hello");

        buffer.rewind();
        Recoverable.DataHeader header2 = proxy.readHeader();
        Assert.assertEquals(header2.revision(),100);
        Assert.assertEquals(header2.factoryId(),10);
        Assert.assertEquals(header2.classId(),1);
        Assert.assertTrue(proxy.readBoolean());
        Assert.assertEquals(proxy.readByte(),(byte)1);
        Assert.assertEquals(proxy.readDouble(),10.0d);
        Assert.assertEquals(proxy.readShort(),4);
        Assert.assertEquals(proxy.readInt(),100);
        Assert.assertEquals(proxy.readFloat(),99f);
        Assert.assertEquals(proxy.readLong(),800l);
        Assert.assertEquals(proxy.readUTF8(),"hello");
    }
    @Test(groups = { "bufferProxy" })
    public void bufferToArrayTest() {
        Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(200,true);
        dataBuffer.writeUTF8("test").writeInt(100).writeUTF8("loop");
        dataBuffer.flip();
        byte[] data = dataBuffer.array();
        Recoverable.DataBuffer dataWrap = BufferProxy.wrap(data);
        Assert.assertEquals(dataWrap.readUTF8(),"test");
        Assert.assertEquals(dataWrap.readInt(),100);
        Assert.assertEquals(dataWrap.readUTF8(),"loop");

        ByteBuffer direct = ByteBuffer.allocateDirect(100);
        Recoverable.DataBuffer buffer = BufferProxy.buffer(direct);
        buffer.writeUTF8("test1000");
        direct.flip();
        byte[] ret = buffer.array();
        Assert.assertNotNull(ret);
    }

    @Test(groups = { "bufferProxy" })
    public void bufferUTFNullTest() {
        String n = null;
        Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(200,false);
        dataBuffer.writeUTF8(n).writeInt(100).writeUTF8("loop");
        byte[] data = dataBuffer.array();
        Recoverable.DataBuffer dataWrap = BufferProxy.wrap(data);
        Assert.assertEquals(dataWrap.readUTF8(),null);
        Assert.assertEquals(dataWrap.readInt(),100);
        Assert.assertEquals(dataWrap.readUTF8(),"loop");
    }

    @Test(groups = { "bufferProxy" })
    public void bufferArrayTest(){
        Recoverable.DataBuffer directBuffer = BufferProxy.buffer(100,true);
        directBuffer.writeUTF8("test");
        directBuffer.flip();
        byte[] bytes = directBuffer.array();
        Assert.assertEquals(bytes.length,8);

        Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(100,false);
        dataBuffer.writeUTF8("test");
        dataBuffer.flip();
        byte[] bytes1 = dataBuffer.array();
        Assert.assertEquals(bytes1.length,100);

    }

    @Test(groups = { "bufferProxy" })
    public void bufferWrapTest(){
        Recoverable.DataBuffer directBuffer = BufferProxy.buffer(100,true);
        directBuffer.writeUTF8("test");
        directBuffer.writeLong(100);
        directBuffer.writeInt(200);
        directBuffer.flip();
        byte[] bytes = directBuffer.array();
        Assert.assertEquals(bytes.length,20);

        Recoverable.DataBuffer dataBuffer = BufferProxy.wrapDirectly(bytes);
        dataBuffer.flip();
        Assert.assertEquals(dataBuffer.readUTF8(),"test");
        Assert.assertEquals(dataBuffer.readLong(),100);
        Assert.assertEquals(dataBuffer.readInt(),200);

    }

    @Test(groups = { "bufferProxy" })
    public void bufferClearTest(){
        Recoverable.DataBuffer directBuffer = BufferProxy.buffer(100,true);
        directBuffer.writeUTF8("test");
        directBuffer.flip();
        Assert.assertEquals(directBuffer.readUTF8(),"test");
        directBuffer.clear();
        directBuffer.writeUTF8("clear");
        directBuffer.flip();
        Assert.assertEquals(directBuffer.readUTF8(),"clear");
    }

    @Test(groups = { "bufferProxy" })
    public void bufferRemainingTest(){
        Recoverable.DataBuffer directBuffer = BufferProxy.buffer(100,true);
        directBuffer.writeUTF8("test");
        directBuffer.flip();
        Assert.assertEquals(directBuffer.remaining(),8);
        byte[] ret = directBuffer.array();
        Assert.assertEquals(ret.length,8);

        Recoverable.DataBuffer buffer = BufferProxy.buffer(100,false);
        buffer.writeUTF8("test");
        buffer.flip();
        Assert.assertEquals(buffer.remaining(),8);
        byte[] ret1 = buffer.array();
        Assert.assertEquals(ret1.length,100);
        byte[] arr = Arrays.copyOf(ret1,8);
        Assert.assertEquals(arr.length,8);
        Recoverable.DataBuffer data = BufferProxy.wrap(arr);
        Assert.assertEquals(data.readUTF8(),"test");
    }

    @Test(groups = { "bufferProxy" })
    public void bufferPositionTest(){
        Recoverable.DataBuffer buffer = BufferProxy.buffer(20, true);
        Recoverable.DataHeader header = new LocalHeader(100, 1, 10);
        Exception exception = null;
        try {
            buffer.writeHeader(header);
            buffer.writeInt(2);
            buffer.writeInt(3);
        }catch (Exception ex){
            exception = ex;
            buffer.position(Recoverable.DataHeader.SIZE);
            buffer.writeInt(10);
        }
        Assert.assertNotNull(exception);
        buffer.flip();
        Recoverable.DataHeader h = buffer.readHeader();
        int value = buffer.readInt();
        Assert.assertEquals(h.revision(),header.revision());
        Assert.assertEquals(h.classId(),header.classId());
        Assert.assertEquals(h.factoryId(),header.factoryId());
        Assert.assertEquals(value,10);
    }




}
