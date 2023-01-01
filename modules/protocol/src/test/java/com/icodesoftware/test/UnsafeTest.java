package com.icodesoftware.test;

import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.OffHeapStore;
import com.icodesoftware.util.UnsafeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sun.misc.Unsafe;

public class UnsafeTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "unsafe util" })
    public void offHeapTest() {
        Unsafe unsafe = UnsafeUtil.useUnsafe();
        long mp = unsafe.allocateMemory(MessageBuffer.SIZE);
        Assert.assertTrue(mp>0);
        unsafe.putInt(mp,1);
        unsafe.putInt(mp+4,2);
        int i1 = unsafe.getInt(mp);
        int i2 = unsafe.getInt(mp+4);
        unsafe.freeMemory(mp);
        Assert.assertEquals(i1,1);
        Assert.assertEquals(i2,2);
    }

    @Test(groups = { "unsafe util" })
    public void offHeapMapTest() {
        String key = "abc";
        OffHeapStore offHeapMap = new OffHeapStore();
        Assert.assertNull(offHeapMap.get(key));
        byte[] value = "test12".getBytes();
        offHeapMap.set(key,value);
        byte[] cached = offHeapMap.get(key);
        Assert.assertEquals(value,cached);
        byte[] updates = "test212".getBytes();
        offHeapMap.set(key,updates);
        Assert.assertEquals(updates,offHeapMap.get(key));
        offHeapMap.clear();
        Assert.assertNull(offHeapMap.get(key));
    }

}
