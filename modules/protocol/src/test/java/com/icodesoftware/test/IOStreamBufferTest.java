package com.icodesoftware.test;

import com.beust.ah.A;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.DataBufferInputStream;
import com.icodesoftware.util.IOStreamDataBuffer;
import com.icodesoftware.util.LocalHeader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class IOStreamBufferTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "buffer" })
    public void inputStreamTest() {
        Exception exception = null;
        try {
            ByteArrayOutputStream dest = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(dest);
            out.writeLong(100);
            out.writeBoolean(true);
            out.writeInt(199);
            out.writeShort(12);
            out.writeDouble(12.0);
            out.writeFloat(10.1f);
            out.writeUTF("utfstring");
            out.writeByte(1);
            byte[] hb = "hello".getBytes();
            for(byte b : hb){
                out.writeByte(b);
            }
            Recoverable.DataBuffer buffer = IOStreamDataBuffer.reader(new ByteArrayInputStream(dest.toByteArray()));
            Assert.assertEquals(buffer.readLong(),100);
            Assert.assertTrue(buffer.readBoolean());
            Assert.assertEquals(buffer.readInt(),199);
            Assert.assertEquals(buffer.readShort(),12);
            Assert.assertEquals(buffer.readDouble(),12.0);
            Assert.assertEquals(buffer.readFloat(),10.1f);
            Assert.assertEquals(buffer.readUTF8(),"utfstring");
            Assert.assertEquals(buffer.readByte(),1);
            Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(5,false);
            buffer.read(dataBuffer);
            dataBuffer.flip();
            Assert.assertEquals(new String(dataBuffer.array()),"hello");
        }catch (Exception ex){
            exception = ex;
        }
        Assert.assertNull(exception);
    }

    @Test(groups = { "buffer" })
    public void outputStreamTest() {
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        Recoverable.DataBuffer buffer = IOStreamDataBuffer.writer(dest);
        buffer.writeHeader(new LocalHeader(100,10,20));
        buffer.writeInt(100).writeUTF8("test").writeShort((short) 2);
        buffer.writeBoolean(false).writeLong(1000).writeDouble(10.3D).writeFloat(2.1f);
        buffer.writeByte((byte) 3);
        Recoverable.DataBuffer src = BufferProxy.buffer(5,false);
        byte[] hb = "hello".getBytes();
        for(byte b : hb){
            src.writeByte(b);
        }
        src.flip();
        buffer.write(src);
        Recoverable.DataBuffer reader = IOStreamDataBuffer.reader(new ByteArrayInputStream(dest.toByteArray()));
        Recoverable.DataHeader header = reader.readHeader();
        Assert.assertEquals(header.revision(),100);
        Assert.assertEquals(header.factoryId(),10);
        Assert.assertEquals(header.classId(),20);
        Assert.assertEquals(reader.readInt(),100);
        Assert.assertEquals(reader.readUTF8(),"test");
        Assert.assertEquals(reader.readShort(),2);
        Assert.assertFalse(reader.readBoolean());
        Assert.assertEquals(reader.readLong(),1000);
        Assert.assertEquals(reader.readDouble(),10.3D);
        Assert.assertEquals(reader.readFloat(),2.1f);
        Assert.assertEquals(reader.readByte(),3);
        Recoverable.DataBuffer dest1 = BufferProxy.buffer(5,false);
        reader.read(dest1);
        Assert.assertEquals(new String(dest1.array()),"hello");
    }
}
