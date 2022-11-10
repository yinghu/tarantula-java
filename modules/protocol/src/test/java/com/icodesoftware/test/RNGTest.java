package com.icodesoftware.test;

import com.icodesoftware.service.RNG;
import com.icodesoftware.util.JvmRNG;
import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

public class RNGTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "rng util" })
    public void midnightTest() {
        RNG rng = new JvmRNG();
        int nx = rng.onNext(3);
        Assert.assertTrue(nx<3);
        Assert.assertTrue(nx>=0);
    }

}
