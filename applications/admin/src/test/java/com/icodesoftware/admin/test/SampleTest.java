package com.icodesoftware.admin.test;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleTest {

    @Test(groups = { "sample test" })
    public void sampleTest() {
        Assert.assertEquals(1,1);
    }
}
