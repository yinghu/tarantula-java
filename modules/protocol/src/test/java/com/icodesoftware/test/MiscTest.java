package com.icodesoftware.test;


import com.icodesoftware.service.RNG;
import com.icodesoftware.util.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.nio.ByteBuffer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MiscTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "misc test" })
    public void rngTest() {
        RNG rng = new JvmRNG();
        int nx = rng.onNext(3);
        Assert.assertTrue(nx<3);
        Assert.assertTrue(nx>=0);
    }

    @Test(groups = { "misc test" })
    public void bufferTest() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(100);
        buffer.put("data".getBytes()).flip();
        Assert.assertEquals(UTF_8.decode(buffer).length(),4);
        buffer.rewind();
        Assert.assertEquals(UTF_8.decode(buffer).length(),4);
    }

    @Test(groups = { "misc test" })
    public void bufferEqualTest() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(100);
        buffer.put("data".getBytes()).flip();
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(10);
        buffer2.put("data".getBytes()).flip();
        Assert.assertTrue(BufferUtil.equals(buffer,buffer2));
    }

    @Test(groups = { "misc test" })
    public void bufferNotEqualTest() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(100);
        buffer.put("data".getBytes()).flip();
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(10);
        buffer2.put("data1".getBytes()).flip();
        Assert.assertFalse(BufferUtil.equals(buffer,buffer2));
    }

    @Test(groups = { "misc test" })
    public void snowflakeTest() {
        long epochStart = TimeUtil.epochMillisecondsFromMidnight(2020,1,1);
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(100,epochStart);
        ConcurrentHashMap<Long,Long> unique = new ConcurrentHashMap<>();
        for(int i=0;i<10000;i++){
            Assert.assertNull(unique.putIfAbsent(snowflakeIdGenerator.snowflakeId(),1L));
        }
    }
    @Test(groups = { "misc test" })
    public void bufferUtilTest() {
        byte[] data = BufferUtil.fromLong(100);
        Assert.assertEquals(BufferUtil.toLong(data),100);
        SnowflakeKey key = new SnowflakeKey(1000L);
        Assert.assertEquals(BufferUtil.toLong(key.asBinary()),1000);
        BinaryKey binaryKey = new BinaryKey(BufferUtil.fromLong("500"));
        Assert.assertEquals(binaryKey.key,BufferUtil.fromLong(500));
    }

    @Test(groups = { "misc test" })
    public void modeTest() {
        int[] v1 = {1,2,3,4,5,6,7,8,9};
        Assert.assertEquals(v1[0]%10,1);
        Assert.assertEquals(v1[1]%10,2);
        Assert.assertEquals(v1[2]%10,3);
        Assert.assertEquals(v1[3]%10,4);
        Assert.assertEquals(v1[4]%10,5);
        Assert.assertEquals(v1[5]%10,6);
        Assert.assertEquals(v1[6]%10,7);
        Assert.assertEquals(v1[7]%10,8);
        Assert.assertEquals(v1[8]%10,9);

        int[] v10 = {11,12,13,14,15,16,17,18,19};
        Assert.assertEquals(v10[0]%10,1);
        Assert.assertEquals(v10[1]%10,2);
        Assert.assertEquals(v10[2]%10,3);
        Assert.assertEquals(v10[3]%10,4);
        Assert.assertEquals(v10[4]%10,5);
        Assert.assertEquals(v10[5]%10,6);
        Assert.assertEquals(v10[6]%10,7);
        Assert.assertEquals(v10[7]%10,8);
        Assert.assertEquals(v10[8]%10,9);

        int[] v20 = {21,22,23,24,25,26,27,28,29};
        Assert.assertEquals(v20[0]%20,1);
        Assert.assertEquals(v20[1]%20,2);
        Assert.assertEquals(v20[2]%20,3);
        Assert.assertEquals(v20[3]%20,4);
        Assert.assertEquals(v20[4]%20,5);
        Assert.assertEquals(v20[5]%20,6);
        Assert.assertEquals(v20[6]%20,7);
        Assert.assertEquals(v20[7]%20,8);
        Assert.assertEquals(v20[8]%20,9);
    }

    @Test(groups = { "misc test" })
    public void mapTest(){
        ConcurrentHashMap<String,String> _m = new ConcurrentHashMap<>();
        AtomicInteger size = new AtomicInteger(0);
        for(int i=0;i<100;i++){
            _m.computeIfAbsent("k"+i,k->{
                if(size.get()>=10) return null;
                size.incrementAndGet();
                return "v"+size.get();
            });
        }
        Assert.assertEquals(_m.size(),10);
    }



}
