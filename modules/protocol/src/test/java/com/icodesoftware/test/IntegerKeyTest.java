package com.icodesoftware.test;

import com.icodesoftware.util.IntegerKey;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntegerKeyTest {

    @Test(groups = { "misc test" })
    public void keyTest() {
        IntegerKey key1 = IntegerKey.from(1);
        Assert.assertEquals(key1.integerId(),1);
    }
}
