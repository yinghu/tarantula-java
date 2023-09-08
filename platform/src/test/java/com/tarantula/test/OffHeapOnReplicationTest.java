package com.tarantula.test;

import com.google.gson.JsonObject;
import com.icodesoftware.service.ClusterProvider;
import com.icodesoftware.service.OnReplication;
import com.tarantula.platform.service.persistence.ClusterNode;
import com.tarantula.platform.service.persistence.OffHeapDataScopeReplication;
import com.tarantula.platform.service.persistence.OffHeapIntegrationScopeReplication;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ArrayBlockingQueue;

public class OffHeapOnReplicationTest {


    @Test(groups = { "OffHeapReplication" })
    public void dataScopeTest() {
        int max = 100;
        ArrayBlockingQueue<OffHeapDataScopeReplication> queue = new ArrayBlockingQueue(max);
        String source = "tarantula_presence";
        ClusterProvider.Node node = new ClusterNode("","N05",12);
        byte[] key = ("bds/" + SystemUtil.oid()).getBytes();
        JsonObject json = new JsonObject();
        json.addProperty("name", "name");
        json.addProperty("password", "password");
        byte[] value = json.toString().getBytes();
        //long st = System.currentTimeMillis();
        for(int i=0;i<max;i++) {
            OffHeapDataScopeReplication offHeapOnReplication = new OffHeapDataScopeReplication();
            offHeapOnReplication.write(node.nodeName(),source, key, value);
            Assert.assertTrue(queue.offer(offHeapOnReplication));
        }
        OffHeapDataScopeReplication unqueued = new OffHeapDataScopeReplication();
        unqueued.write(node.nodeName(),source, key, value);
        Assert.assertFalse(queue.offer(unqueued));
        unqueued.read();
        //long st1 = System.currentTimeMillis();
        //System.out.println("WRITE : "+(st1-st));
        for(int i=0;i<max;i++) {
            OffHeapDataScopeReplication offHeapOnReplication = queue.poll();
            Assert.assertNotNull(offHeapOnReplication);
            OnReplication onReplication = offHeapOnReplication.read();
            Assert.assertEquals(onReplication.nodeName(),node.nodeName());
            Assert.assertEquals(onReplication.source(),source);
            Assert.assertEquals(onReplication.key(),key);
            Assert.assertEquals(onReplication.value(),value);
        }
        //System.out.println("READ : "+(System.currentTimeMillis()-st1));
    }

    @Test(groups = { "OffHeapReplication" })
    public void integrationScopeTest() {
        int max = 100;
        ArrayBlockingQueue<OffHeapIntegrationScopeReplication> queue = new ArrayBlockingQueue(max);
        int partition = 10;
        ClusterProvider.Node node = new ClusterNode("","N05",12);
        byte[] key = ("bds/" + SystemUtil.oid()).getBytes();
        JsonObject json = new JsonObject();
        json.addProperty("name", "name");
        json.addProperty("password", "password");
        byte[] value = json.toString().getBytes();
        //long st = System.currentTimeMillis();
        for(int i=0;i<max;i++) {
            OffHeapIntegrationScopeReplication offHeapOnReplication = new OffHeapIntegrationScopeReplication();
            offHeapOnReplication.write(node.nodeName(),partition, key, value);
            Assert.assertTrue(queue.offer(offHeapOnReplication));
        }
        OffHeapIntegrationScopeReplication unqueued = new OffHeapIntegrationScopeReplication();
        unqueued.write(node.nodeName(),partition, key, value);
        Assert.assertFalse(queue.offer(unqueued));
        unqueued.read();
        //long st1 = System.currentTimeMillis();
        //System.out.println("WRITE : "+(st1-st));
        for(int i=0;i<max;i++) {
            OffHeapIntegrationScopeReplication offHeapOnReplication = queue.poll();
            Assert.assertNotNull(offHeapOnReplication);
            OnReplication onReplication = offHeapOnReplication.read();
            Assert.assertEquals(onReplication.partition(),partition);
            Assert.assertEquals(onReplication.nodeName(),node.nodeName());
            Assert.assertEquals(onReplication.key(),key);
            Assert.assertEquals(onReplication.value(),value);
        }
        //System.out.println("READ : "+(System.currentTimeMillis()-st1));
    }
}