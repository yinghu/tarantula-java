package com.icodesoftware.test;

import com.icodesoftware.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

public class TimeUtilTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "time util" })
    public void midnightTest() {
        LocalDateTime mid1 = TimeUtil.toMidnight("2021-09-30");
        Assert.assertEquals(mid1.getDayOfMonth(),30);
        Assert.assertEquals(mid1.getMonthValue(),9);
        LocalDateTime mid = TimeUtil.midnight();
        Assert.assertEquals(mid.getDayOfYear(),LocalDateTime.now().getDayOfYear()+1);
    }

}
