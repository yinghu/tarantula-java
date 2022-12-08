package com.tarantula.test;


import com.icodesoftware.LeaderBoard;
import com.icodesoftware.Property;
import com.icodesoftware.service.Metrics;
import com.tarantula.platform.service.metrics.MetricsProperty;
import com.tarantula.platform.service.metrics.PerformanceMetrics;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MetricsTest {

    @BeforeClass
    public void setUp() {
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsYearlyTest() {
        TestServiceContext serviceContext = new TestServiceContext();
        LocalDateTime end = LocalDate.parse("2022-12-31").atTime(23,50,0,0);
        MockMetrics metrics = new MockMetrics(end);
        Assert.assertEquals(end.getHour()==23,true);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.run();//update statistics entry
        Property[] mc = metrics.snapshot(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT, LeaderBoard.HOURLY);
        Assert.assertEquals(Double.parseDouble(mc[11].value().toString()) ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atHourly();//swap statistics
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).monthly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).yearly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsMonthlyTest() {
        TestServiceContext serviceContext = new TestServiceContext();
        LocalDateTime end = LocalDate.parse("2022-07-31").atTime(23,50,0,0);//Mon
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atHourly();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).monthly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsWeeklyTest() {
        TestServiceContext serviceContext = new TestServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-07").atTime(23,50,0,0);//Mon
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atHourly();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).monthly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsDailyTest() {
        TestServiceContext serviceContext = new TestServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-10").atTime(23,50,0,0);//Sun
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atHourly();
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).monthly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsHistoryTest() {
        TestServiceContext serviceContext = new TestServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-07").atTime(23,50,0,0);
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.atHourly();
        Metrics.History[] history = metrics.archive(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,LeaderBoard.HOURLY,end);
        Property[] his = history[0].hourlyGain();
        String h12 = MetricsProperty.historyPropertyLabel(end);
        Property archived = null;
        for(Property p : his){
            if(p.name().equals(h12)){
                archived = p;
                break;
            }
        }
        Assert.assertTrue(archived!=null);
        Assert.assertEquals(archived.value(),3.0);
        Assert.assertEquals(history[0].dailyGain(),3.0);
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
