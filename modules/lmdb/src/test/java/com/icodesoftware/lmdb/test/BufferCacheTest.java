package com.icodesoftware.lmdb.test;

import com.icodesoftware.lmdb.BufferCache;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class BufferCacheTest {

    final ArrayBlockingQueue<BufferCache> limitedCache = new ArrayBlockingQueue<>(10);

    @BeforeClass
    public void setUp(){

    }

    @Test(groups = { "bufferCache" })
    public void bufferCacheOnThread(){
        CountDownLatch c100 = new CountDownLatch(100);
        for(int i=0;i<100;i++){
            new Thread(()->{
                try(BufferCache cache = borrow()){
                    Assert.assertNotNull(cache.key());
                    Assert.assertNotNull(cache.value());
                    c100.countDown();
                }
            }).start();
        }
        try{c100.await();}catch (Exception ex){}
        Assert.assertTrue(limitedCache.size()<=10);
    }

    private BufferCache borrow(){
        BufferCache cache = limitedCache.poll();
        if(cache!=null) return cache;
        return new BufferCache(100,500,limitedCache);
    }
}
