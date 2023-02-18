package com.icodesoftware.test;

import com.icodesoftware.Tournament;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EnumTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "enum" })
    public void enumTest() {
        Tournament.Status closed = Tournament.Status.CLOSED;
        Assert.assertTrue(Tournament.Status.CLOSED==closed);
        Assert.assertTrue(closed != Tournament.Status.PENDING);
    }

}
