package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.Property;
import com.icodesoftware.Statistics;
import com.icodesoftware.service.Metrics;
import com.tarantula.platform.service.metrics.*;

import com.tarantula.platform.statistics.StatisticsPortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MetricsTest extends DataStoreHook{


    @Test(groups = { "PerformanceMetrics" })
    public void metricsSetupTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_metrics_"+Metrics.SYSTEM);
        SystemStatistics statistics = new SystemStatistics();
        statistics.distributionId(serviceContext.distributionId());
        statistics.label(Metrics.SYSTEM);
        statistics.dataStore(dataStore);
        statistics.load();
        Assert.assertEquals(statistics.summary().size(),0);
        statistics.entry(SystemMetrics.ACCESS_AMAZON_S3_COUNT).update(1).update();
        statistics.entry(SystemMetrics.ACCESS_GOOGLE_LOGIN_COUNT).update(1).update();
        Assert.assertEquals(statistics.summary().size(),2);

        SystemStatistics load = new SystemStatistics();
        load.distributionId(statistics.distributionId());
        load.label(Metrics.SYSTEM);
        load.dataStore(dataStore);
        load.load();
        Assert.assertEquals(load.summary().size(),2);
        load.summary().forEach(e->{
            Assert.assertNotNull(e.name());
            Assert.assertEquals(e.total(),1.0D);
            Assert.assertEquals(e.hourly(),1.0D);
            Assert.assertEquals(e.daily(),1.0D);
            Assert.assertEquals(e.weekly(),1.0D);
            Assert.assertEquals(e.monthly(),1.0D);
            Assert.assertEquals(e.yearly(),1.0D);
        });
        LocalDateTime localDateTime = LocalDateTime.now();
        for(int i=1;i<=12;i++) {
            MetricsHistory metricsHistory = new MetricsHistory(SystemMetrics.ACCESS_AMAZON_S3_COUNT, localDateTime.getYear(),i);
            metricsHistory.ownerKey(statistics.key());
            Assert.assertTrue(dataStore.create(metricsHistory));
        }
        RecoverableQuery<MetricsHistory> query = RecoverableQuery.query(statistics.distributionId(),new MetricsHistory(SystemMetrics.ACCESS_AMAZON_S3_COUNT, localDateTime.getYear(),1), StatisticsPortableRegistry.INS);
        Assert.assertEquals(dataStore.list(query).size(),12);
        for(int i=1;i<=12;i++){
            Assert.assertNotNull(statistics.loadMetricsHistory(12));
        }
        Assert.assertNull(statistics.loadMetricsHistory(13));

        MetricsSnapshot metricsSnapshot = new MetricsSnapshot(SystemMetrics.ACCESS_AMAZON_S3_COUNT,LeaderBoard.HOURLY);
        metricsSnapshot.ownerKey(statistics.key());

        //metricsSnapshot.initialize(new MetricsProperty());
        //System.out.println(MetricsSnapshot.hourlyLabel(LocalDateTime.now()));
        Assert.assertTrue(dataStore.create(metricsSnapshot));

        MetricsSnapshot metricsSnapshot1 = new MetricsSnapshot(SystemMetrics.ACCESS_AMAZON_S3_COUNT,LeaderBoard.DAILY);
        metricsSnapshot1.ownerKey(statistics.key());
        Assert.assertTrue(dataStore.create(metricsSnapshot1));

        Assert.assertNotNull(statistics.loadMetricsSnapshot(LeaderBoard.HOURLY));
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsYearlyTest() {

        LocalDateTime end = LocalDate.parse("2022-12-31").atTime(23,50,0,0);
        MockMetrics metrics = new MockMetrics(end);
        Assert.assertEquals(end.getHour()==23,true);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1D);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,2D);
        metrics.run();//update statistics entry
        Metrics.Spot[] mc = metrics.snapshot(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT, LeaderBoard.HOURLY);
        //for(Property p : mc){
            //System.out.println(p.name());
        //}
        Assert.assertEquals((mc[11].value()) ==3,true);
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

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsWeeklyTest() {

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

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsDailyTest() {

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

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsHistoryTest() {

        LocalDateTime end = LocalDate.parse("2022-08-07").atTime(23,50,0,0);
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.atHourly();
        Metrics.History history = metrics.archive(PerformanceMetrics.PERFORMANCE_HTTP_REQUEST_COUNT,end);
        Metrics.Spot[] his = history.hourlyGain();
        String h12 = MetricsProperty.historyPropertyLabel(end);
        Metrics.Spot archived = null;
        for(Metrics.Spot p : his){
            //System.out.println(p.name()+">>>"+p.value());
            if(p.name().equals(h12)){
                archived = p;
                break;
            }
        }
        Assert.assertTrue(archived!=null);
        Assert.assertEquals(archived.value(),3.0);
        Assert.assertEquals(history.dailyGain(),3.0);
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
