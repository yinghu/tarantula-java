package com.tarantula.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerTest {


    @Test(groups = { "AtomicInteger" })
    public void atomicOperationTest() {
        AtomicInteger limit = new AtomicInteger(1);
        int xp = limit.getAndAccumulate(1,(x,y)->{
            //x = existing value
            //y = updating value
            return x+y;
        });

        //increase 1
        Assert.assertEquals(xp,1);
        Assert.assertEquals(limit.get(),2);

        //decrease 1
        int dp = limit.getAndAccumulate(1,(x,y)->x-y);
        Assert.assertEquals(dp,2);
        Assert.assertEquals(limit.get(),1);

        //reset 0
        int zp = limit.getAndAccumulate(1,(x,y)->0);
        Assert.assertEquals(zp,1);
        Assert.assertEquals(limit.get(),0);

    }

}
