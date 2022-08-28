package com.tarantula.test;


import com.tarantula.platform.service.metrics.PerformanceMetrics;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MetricsTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsYearlyTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-01-01").atTime(LocalTime.MIDNIGHT);//Sat
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.HTTP_REQUEST_COUNT,2);
        metrics.run();//update statistics entry
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atMidnight();//swap statistics
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).monthly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).yearly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).total()==0,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsMonthlyTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-01").atTime(LocalTime.MIDNIGHT);//Mon
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atMidnight();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).monthly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).total()==3,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsWeeklyTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-07").atTime(LocalTime.MIDNIGHT);//Sun
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atMidnight();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).weekly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).monthly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).total()==3,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsDailyTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-10").atTime(LocalTime.MIDNIGHT);//Sun
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atMidnight();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).monthly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.HTTP_REQUEST_COUNT).total()==3,true);
    }

    private class MockMetrics extends PerformanceMetrics{

        LocalDateTime end;
        public MockMetrics(LocalDateTime end){
            this.end = end;
        }
        @Override
        protected LocalDateTime end() {
            return end;
        }
    }


}
