package com.icodesoftware.test;


import com.icodesoftware.util.StringUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StringUtilTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "string util" })
    public void snakeCaseTest() {
        String[] path = {"c","Boss","Moon"};
        String snake = StringUtil.toSnakeCase(path);
        Assert.assertEquals(snake,"c_boss_moon");
    }

}
