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
        Assert.assertEquals(mid.getDayOfYear(),LocalDateTime.now().plusDays(1).getDayOfYear());
    }

    @Test(groups = { "time util" })
    public void toMondayTest() {
        LocalDateTime _cur = LocalDateTime.now();
        LocalDateTime w1 = TimeUtil.toLastMonday(_cur);
        LocalDateTime m1 = TimeUtil.toFirstDayOfLastMonth(_cur);
        Assert.assertTrue(w1.getDayOfWeek().getValue()==1);
        Assert.assertTrue(TimeUtil.toLastMonday(w1).getDayOfWeek().getValue()==1);
        Assert.assertTrue(m1.getDayOfMonth()==1);
        Assert.assertTrue(m1.getMonth().getValue()!=_cur.getMonth().getValue()+1);
    }

}
