package com.icodesoftware.test;

import com.icodesoftware.util.FIFOBuffer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FIFOBufferTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "fifo buffer" })
    public void payloadBufferTest() {
        FIFOBuffer<Integer> fifo = new FIFOBuffer<>(10,new Integer[10]);
        for(int i=0;i<20;i++){
            fifo.push(i);
        }
        Integer[] ret = new Integer[10];
        fifo.list(ret);
        for(int i=0;i<10;i++){
            int t = fifo.pop();
            Assert.assertTrue(t==ret[i]);
        }
    }

}
