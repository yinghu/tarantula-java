package com.tarantula.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockQueueDrainTest {


    @Test(groups = { "BlockingQueue" })
    public void drainOperationTest() {
        ArrayBlockingQueue<String> limit = new ArrayBlockingQueue(100);
        for(int i=0;i<100;i++){
            Assert.assertTrue(limit.offer("Q"+i));
        }
        Assert.assertFalse(limit.offer("failed"));
        ArrayList<String> d10 = new ArrayList<>(10);
        Assert.assertEquals(limit.drainTo(d10,10),10);
        for(int i=0;i<10;i++){
            Assert.assertEquals(d10.get(i),"Q"+i);
        }
        //d10.clear();
        Assert.assertEquals(limit.drainTo(d10,90),90);
        for(int i=0;i<100;i++){
            Assert.assertEquals(d10.get(i),"Q"+i);
        }
    }

}
