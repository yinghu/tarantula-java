package com.icodesoftware.tournament.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class StackTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "Tournament" })
    public void stackInitTest() {
        Assert.assertEquals(1,1);
    }


}
