package com.icodesoftware.test;

import com.icodesoftware.Recoverable;
import com.icodesoftware.util.LocalHeader;
import com.icodesoftware.util.MemorySegmentProxy;

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
            Recoverable.DataBuffer proxy = MemorySegmentProxy.memory(memorySegment);
            Recoverable.DataHeader h = new LocalHeader(10,1,3);
            proxy.writeHeader(h);
            //proxy.writeShort((short)1);
            proxy.writeLong(100L);
            proxy.writeInt(5);
            proxy.flip();
            Recoverable.DataHeader r = proxy.readHeader();
            Assert.assertEquals(h.revision(),r.revision());
            Assert.assertEquals(h.factoryId(),r.factoryId());
            Assert.assertEquals(h.classId(),r.classId());
            //Assert.assertEquals(proxy.readShort(),1);
            Assert.assertEquals(proxy.readLong(),100L);
            Assert.assertEquals(proxy.readInt(),5);
        }

    }


}
