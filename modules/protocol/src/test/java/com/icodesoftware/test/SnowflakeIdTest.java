package com.icodesoftware.test;


import com.icodesoftware.util.SnowflakeIdGenerator;
import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ConcurrentHashMap;

public class SnowflakeIdTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "misc test" })
    public void snowflakeTest() {
        long epochStart = TimeUtil.epochMillisecondsFromMidnight(2020,1,1);
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(100,epochStart);
        ConcurrentHashMap<Long,Long> unique = new ConcurrentHashMap<>();
        for(int i=0;i<10;i++){
            Assert.assertNull(unique.putIfAbsent(snowflakeIdGenerator.snowflakeId(),1L));
        }
        unique.forEach((k,v)->{
            long[] bits = snowflakeIdGenerator.fromSnowflakeId(k);
            System.out.println(TimeUtil.fromUTCMilliseconds(bits[0]));
            for(int i=0;i<bits.length;i++){
                System.out.println("B1=>"+bits[i]);
            }
        });

    }
}
