package com.icodesoftware.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;
import com.icodesoftware.util.LocalHeader;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;


public class MemorySegmentProxyTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "memory segment proxy" })
    public void memoryProxyTest() {
        try(Arena arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(1000);
            Recoverable.DataBuffer proxy = BufferProxy.buffer(memorySegment);
            Recoverable.DataHeader h = new LocalHeader(10,1,3);
            proxy.writeHeader(h);
            proxy.writeShort((short)1);
            proxy.writeLong(100L);
            proxy.writeInt(5);
            proxy.writeBoolean(true);
            proxy.writeUTF8("hello");
            proxy.flip();
            Recoverable.DataHeader r = proxy.readHeader();
            Assert.assertEquals(h.revision(),r.revision());
            Assert.assertEquals(h.factoryId(),r.factoryId());
            Assert.assertEquals(h.classId(),r.classId());
            Assert.assertEquals(proxy.readShort(),1);
            Assert.assertEquals(proxy.readLong(),100L);
            Assert.assertEquals(proxy.readInt(),5);
            Assert.assertTrue(proxy.readBoolean());
            Assert.assertEquals(proxy.readUTF8(),"hello");
            proxy.rewind();
            Recoverable.DataHeader x = proxy.readHeader();
            Assert.assertEquals(h.revision(),x.revision());
            Assert.assertEquals(h.factoryId(),x.factoryId());
            Assert.assertEquals(h.classId(),x.classId());
            Assert.assertEquals(proxy.readShort(),1);
            Assert.assertEquals(proxy.readLong(),100L);
            Assert.assertEquals(proxy.readInt(),5);
            Assert.assertTrue(proxy.readBoolean());
            Assert.assertEquals(proxy.readUTF8(),"hello");
            Assert.assertTrue(proxy.src().isDirect());
            //copy out
            MemorySegment copy = arena.allocate(1000);
            copy.copyFrom(memorySegment);
            Recoverable.DataBuffer buffer = BufferProxy.buffer(copy);
            Recoverable.DataHeader hx = buffer.readHeader();
            Assert.assertEquals(hx.revision(),r.revision());
            Assert.assertEquals(hx.factoryId(),r.factoryId());
            Assert.assertEquals(hx.classId(),r.classId());
            Assert.assertEquals(buffer.readShort(),1);
            Assert.assertEquals(buffer.readLong(),100L);
            Assert.assertEquals(buffer.readInt(),5);
            Assert.assertTrue(buffer.readBoolean());
            Assert.assertEquals(buffer.readUTF8(),"hello");
            Assert.assertNotNull(buffer.pointer());
        }

    }


}
