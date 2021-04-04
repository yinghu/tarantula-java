package com.tarantula.test.util;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SystemUtilTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "SystemUtil" })
    public void payloadBufferTest() {
        Assert.assertEquals(1,1);
    }
}
