package com.icodesoftware.lmdb.test;


import com.icodesoftware.Recoverable;
import com.icodesoftware.util.BufferProxy;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;


public class EndiannessTest {

    @Test(groups = { "Endian Test" })
    public void bigEndianTest(){
        try(Arena arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(100);
            memorySegment.set(ValueLayout.JAVA_LONG.withOrder(ByteOrder.BIG_ENDIAN),0,189);
            memorySegment.set(ValueLayout.JAVA_LONG.withOrder(ByteOrder.BIG_ENDIAN),8,234);
            Recoverable.DataBuffer reader = BufferProxy.buffer(memorySegment);
            Assert.assertEquals(reader.readLong(),189L);
            Assert.assertEquals(reader.readLong(),234L);
            Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(100,false);
            for(long i=0;i<16;i++){
                dataBuffer.writeByte(memorySegment.get(ValueLayout.JAVA_BYTE,i));
            }
            dataBuffer.flip();
            Assert.assertEquals(dataBuffer.readLong(),189L);
            Assert.assertEquals(dataBuffer.readLong(),234L);
        }
    }

    @Test(groups = { "Endian Test" })
    public void littleEndianTest(){
        try(Arena arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(100);
            memorySegment.set(ValueLayout.JAVA_LONG.withOrder(ByteOrder.LITTLE_ENDIAN),0,189);
            memorySegment.set(ValueLayout.JAVA_LONG.withOrder(ByteOrder.LITTLE_ENDIAN),8,234);
            Recoverable.DataBuffer reader = BufferProxy.buffer(memorySegment);
            reader.src().order(ByteOrder.LITTLE_ENDIAN);
            Assert.assertEquals(reader.readLong(),189L);
            Assert.assertEquals(reader.readLong(),234L);
            Recoverable.DataBuffer dataBuffer = BufferProxy.buffer(100,false);
            dataBuffer.src().order(ByteOrder.LITTLE_ENDIAN);
            for(long i=0;i<16;i++){
                dataBuffer.writeByte(memorySegment.get(ValueLayout.JAVA_BYTE,i));
            }
            dataBuffer.flip();
            Assert.assertEquals(dataBuffer.readLong(),189L);
            Assert.assertEquals(dataBuffer.readLong(),234L);
        }
    }
}
