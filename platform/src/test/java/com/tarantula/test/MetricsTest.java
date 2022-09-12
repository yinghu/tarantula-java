package com.tarantula.test;


import com.icodesoftware.LeaderBoard;
import com.icodesoftware.Property;
import com.tarantula.platform.service.metrics.MetricsProperty;
import com.tarantula.platform.service.metrics.MetricsSnapshot;
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

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsYearlyTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-01-01").atTime(LocalTime.MIDNIGHT);//Sat
        MockMetrics metrics = new MockMetrics(end);
        Assert.assertEquals(end.getHour()==0,true);
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

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsMonthlyTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-01").atTime(LocalTime.MIDNIGHT);//Mon
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

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsWeeklyTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-08").atTime(LocalTime.MIDNIGHT);//Mon
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

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsDailyTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-10").atTime(LocalTime.MIDNIGHT);//Sun
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

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsHistoryTest() {
        EmptyServiceContext serviceContext = new EmptyServiceContext();
        LocalDateTime end = LocalDate.parse("2022-08-07").atTime(LocalTime.MIDNIGHT).minusHours(1);
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.atHourly();
        Property[] his = metrics.history(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,end,end);
        String hkey = MetricsProperty.historyPropertyLabel(end);
        Assert.assertEquals(his[0].name().equals(hkey),true);
        Assert.assertEquals(his[1]==null,true);
    }

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsHistoryPushTest() {

        LocalDateTime end = LocalDate.parse("2022-08-07").atTime(LocalTime.MIDNIGHT).minusHours(1);
        MetricsSnapshot metricsSnapshot = new MetricsSnapshot(24,"category","hourly");
        for(int i=0;i<24;i++){
            metricsSnapshot.initialize(new MetricsProperty(i,"m"+i,i,end),end);
        }
        for(int i=0;i<24;i++){
            Property p = metricsSnapshot.push(new MetricsProperty(100+i,"x"+i,100,end),end);
            Assert.assertEquals(p.name().equals("m"+i),true);
            Assert.assertEquals(p.value().equals(i),true);
        }
        Property[] props = metricsSnapshot.metrics();
        for(int i=0;i<24;i++){
            Assert.assertEquals(props[i].name().equals("x"+i),true);
            Assert.assertEquals(props[i].value().equals(100),true);
        }
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
