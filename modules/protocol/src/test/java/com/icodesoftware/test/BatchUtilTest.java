package com.icodesoftware.test;

import com.icodesoftware.protocol.MessageBuffer;
import com.icodesoftware.util.BatchUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class BatchUtilTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "batch util" })
    public void LessBatchTest() {
        BatchUtil.Batch batch = BatchUtil.batch(100,MessageBuffer.PAYLOAD_SIZE);
        Assert.assertEquals(batch.offsets.size(),batch.size);
        Assert.assertEquals(1,batch.offsets.get(0).batch);
        Assert.assertEquals(0,batch.offsets.get(0).offset);
        Assert.assertEquals(100,batch.offsets.get(0).length);
    }

    @Test(groups = { "batch util" })
    public void FullBatchTest() {
        BatchUtil.Batch batch = BatchUtil.batch(MessageBuffer.PAYLOAD_SIZE*3,MessageBuffer.PAYLOAD_SIZE);
        Assert.assertEquals(batch.offsets.size(),batch.size);
        Assert.assertEquals(3,batch.offsets.get(2).batch);
        Assert.assertEquals(MessageBuffer.PAYLOAD_SIZE*2,batch.offsets.get(2).offset);
        Assert.assertEquals(MessageBuffer.PAYLOAD_SIZE,batch.offsets.get(2).length);
    }

    @Test(groups = { "batch util" })
    public void NotFullBatchTest() {
        BatchUtil.Batch batch = BatchUtil.batch(MessageBuffer.PAYLOAD_SIZE*2+100,MessageBuffer.PAYLOAD_SIZE);
        Assert.assertEquals(batch.offsets.size(),batch.size);
        Assert.assertEquals(3,batch.offsets.get(2).batch);
        Assert.assertEquals(MessageBuffer.PAYLOAD_SIZE*2,batch.offsets.get(2).offset);
        Assert.assertEquals(100,batch.offsets.get(2).length);
    }
}
