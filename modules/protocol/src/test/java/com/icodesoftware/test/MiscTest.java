package com.icodesoftware.test;

import com.icodesoftware.service.RNG;
import com.icodesoftware.util.BufferUtil;
import com.icodesoftware.util.JvmRNG;
import com.icodesoftware.util.SnowflakeIdGenerator;
import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;

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
}
