package com.tarantula.test;

import com.google.gson.JsonObject;
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.service.persistence.berkeley.OffHeapOnReplication;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ArrayBlockingQueue;

public class OffHeapOnReplicationTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "OffHeapReplication" })
    public void localTest() {
        int max = 100;
        ArrayBlockingQueue<OffHeapOnReplication> queue = new ArrayBlockingQueue(max);
        String source = "tarantula_presence";

        byte[] key = ("bds/" + SystemUtil.oid()).getBytes();
        JsonObject json = new JsonObject();
        json.addProperty("name", "name");
        json.addProperty("password", "password");
        byte[] value = json.toString().getBytes();
        //long st = System.currentTimeMillis();
        for(int i=0;i<max;i++) {
            OffHeapOnReplication offHeapOnReplication = new OffHeapOnReplication(source, key, value);
            Assert.assertTrue(queue.offer(offHeapOnReplication));
        }
        OffHeapOnReplication unqueued = new OffHeapOnReplication(source, key, value);
        Assert.assertFalse(queue.offer(unqueued));
        unqueued.readOffHeap();
        //long st1 = System.currentTimeMillis();
        //System.out.println("WRITE : "+(st1-st));
        for(int i=0;i<max;i++) {
            OffHeapOnReplication offHeapOnReplication = queue.poll();
            Assert.assertNotNull(offHeapOnReplication);
            OnReplication onReplication = offHeapOnReplication.readOffHeap();
            Assert.assertEquals(onReplication.source(),source);
            Assert.assertEquals(onReplication.key(),key);
            Assert.assertEquals(onReplication.value(),value);
        }
        //System.out.println("READ : "+(System.currentTimeMillis()-st1));
    }
}