package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Distributable;
import com.icodesoftware.LeaderBoard;
import com.icodesoftware.service.Metrics;

import com.tarantula.platform.service.metrics.*;

import com.tarantula.platform.statistics.StatisticsPortableRegistry;
import com.tarantula.platform.util.RecoverableQuery;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MetricsTest extends DataStoreHook{

    public final static String PERFORMANCE_HTTP_REQUEST_COUNT = "httpRequestCount";
    @Test(groups = { "PerformanceMetrics" })
    public void metricsSetupTest() {
        DataStore dataStore = dataStoreProvider.createLocalDataStore("test_metrics_"+Metrics.SYSTEM);
        SystemStatistics statistics = new SystemStatistics();
        statistics.distributionId(serviceContext.node().nodeId());
        statistics.label(Metrics.SYSTEM);
        statistics.dataStore(dataStore);
        statistics.load();
        Assert.assertEquals(statistics.summary().size(),0);
        statistics.entry(SystemMetrics.ACCESS_AMAZON_S3_COUNT).update(1).update();
        statistics.entry(SystemMetrics.ACCESS_GOOGLE_LOGIN_COUNT).update(1).update();
        statistics.entry(SystemMetrics.ACCESS_DEVELOPER_LOGIN_COUNT).update(1).update();
        statistics.entry("x_kill_count").update(1).update();

        Assert.assertEquals(statistics.summary().size(),4);

        SystemStatistics load = new SystemStatistics();
        load.distributionId(statistics.distributionId());
        load.label(Metrics.SYSTEM);
        load.dataStore(dataStore);
        load.load();
        Assert.assertEquals(load.summary().size(),4);
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
        for(int i=1;i<=10;i++) {
            int day = localDateTime.getDayOfYear()+i;
            MetricsHistory metricsHistory = new MetricsHistory(SystemMetrics.ACCESS_AMAZON_S3_COUNT, localDateTime.getYear(),day);
            metricsHistory.ownerKey(statistics.key());
            metricsHistory.initializeHourly(localDateTime.plusDays(i));
            Assert.assertTrue(dataStore.create(metricsHistory));
        }
        RecoverableQuery<MetricsHistory> query = RecoverableQuery.query(statistics.distributionId(),new MetricsHistory(SystemMetrics.ACCESS_AMAZON_S3_COUNT, localDateTime.getYear(),1), StatisticsPortableRegistry.INS);
        Assert.assertEquals(dataStore.list(query).size(),10);
        for(int i=1;i<=10;i++){
            MetricsHistory history = statistics.loadMetricsHistory(localDateTime.getDayOfYear()+i);
            Assert.assertNotNull(history);
            Assert.assertEquals(history.metrics().length,Metrics.HOURLY_HISTORY_BUFFER_SIZE);
            history.archiveHourly(new MetricsProperty(100,localDateTime));
            history.archiveHourly(new MetricsProperty(100,localDateTime));
            history.archiveDaily(200,localDateTime);
            history.archiveMonthly(200,localDateTime);
            history.archiveWeekly(200,localDateTime);
            history.archiveYearly(200,localDateTime);
            history.update();
            Metrics.History loading = new MetricsHistory();
            loading.distributionId(history.distributionId());
            Assert.assertTrue(dataStore.load(loading));
            Metrics.Spot mp = loading.hourlyGain()[localDateTime.getHour()>0?localDateTime.getHour()-1:(Metrics.HOURLY_HISTORY_BUFFER_SIZE-1)];
            Assert.assertEquals(mp.value(),200.0D);
            Assert.assertEquals(loading.dailyGain(),200.0D);
            Assert.assertEquals(loading.weeklyGain(),200.0D);
            Assert.assertEquals(loading.monthlyGain(),200.0D);
            Assert.assertEquals(loading.yearlyGain(),200.0D);
        }

        MetricsSnapshot metricsSnapshot = new MetricsSnapshot(SystemMetrics.ACCESS_AMAZON_S3_COUNT,LeaderBoard.HOURLY);
        metricsSnapshot.ownerKey(statistics.key());
        for(int i=0;i<Metrics.SNAPSHOT_TRACKING_SIZE;i++){
            LocalDateTime xhf = localDateTime.minusHours(Metrics.SNAPSHOT_TRACKING_SIZE-i);
            String xh = MetricsSnapshot.hourlyLabel(xhf);
            metricsSnapshot.initialize(i,new MetricsProperty(xh,0,xhf),localDateTime);
            Assert.assertNotNull(metricsSnapshot.metrics()[i].name());
        }
        metricsSnapshot.update(100);
        metricsSnapshot.push(new MetricsProperty(MetricsSnapshot.hourlyLabel(localDateTime),200,localDateTime),localDateTime);
        Assert.assertTrue(dataStore.create(metricsSnapshot));
        MetricsSnapshot metricsSnapshot1 = new MetricsSnapshot(SystemMetrics.ACCESS_AMAZON_S3_COUNT,LeaderBoard.DAILY);
        metricsSnapshot1.distributionId(metricsSnapshot.distributionId());
        Assert.assertTrue(dataStore.load(metricsSnapshot1));
        Assert.assertEquals(metricsSnapshot.metrics()[Metrics.SNAPSHOT_TRACKING_SIZE-1].value,metricsSnapshot1.metrics()[Metrics.SNAPSHOT_TRACKING_SIZE-1].value);
        Assert.assertEquals(metricsSnapshot.name(),metricsSnapshot1.name());
        int[] ix={0};
        metricsSnapshot1.reset((p)->{
            LocalDateTime xhf = localDateTime.minusHours(Metrics.SNAPSHOT_TRACKING_SIZE-ix[0]);
            String xh = MetricsSnapshot.hourlyLabel(xhf);
            metricsSnapshot.initialize(ix[0],new MetricsProperty(xh,0,xhf),localDateTime);
            ix[0]++;
            return true;
        });
        //System.out.println(metricsSnapshot1.toJson());
        //Assert.assertNotNull(statistics.loadMetricsSnapshot(LeaderBoard.HOURLY));
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsYearlyTest() {
        DataStore dataStore = serviceContext.dataStore(Distributable.LOCAL_SCOPE,"tarantula_performance_metrics");
        LocalDateTime end = LocalDate.parse("2022-12-31").atTime(23,50,0,0);
        MockMetrics metrics = new MockMetrics(end);
        Assert.assertEquals(end.getHour()==23,true);
        metrics.setup(serviceContext);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,1D);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,2D);
        metrics.run();//update statistics entry
        Metrics.Spot[] mc = metrics.snapshot(PERFORMANCE_HTTP_REQUEST_COUNT, LeaderBoard.HOURLY);
        //for(Metrics.Spot p : mc){
            //System.out.println(p.name()+" : "+p.value());
        //}
        Assert.assertEquals((mc[11].value()),3.0d);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atHourly();//swap statistics
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).monthly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).yearly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);

    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsMonthlyTest() {

        LocalDateTime end = LocalDate.parse("2022-07-31").atTime(23,50,0,0);//Mon
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atHourly();
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).monthly() ==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsWeeklyTest() {

        LocalDateTime end = LocalDate.parse("2022-08-07").atTime(23,50,0,0);//Mon
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atHourly();
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).monthly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
    }

    @Test(groups = { "PerformanceMetrics" })
    public void metricsDailyTest() {

        LocalDateTime end = LocalDate.parse("2022-08-10").atTime(23,50,0,0);//Sun
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.run();
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).daily()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).monthly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).yearly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
        metrics.atHourly();
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).hourly()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).daily()==0,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).weekly()==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).monthly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).yearly() ==3,true);
        Assert.assertEquals(metrics.statistics().entry(PERFORMANCE_HTTP_REQUEST_COUNT).total()==3,true);
    }

    //@Test(groups = { "PerformanceMetrics" })
    public void metricsHistoryTest() {

        LocalDateTime end = LocalDate.parse("2022-08-07").atTime(23,50,0,0);
        MockMetrics metrics = new MockMetrics(end);
        metrics.setup(serviceContext);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,1);
        metrics.onUpdated(PERFORMANCE_HTTP_REQUEST_COUNT,2);
        metrics.atHourly();
        Metrics.History history = metrics.archive(PERFORMANCE_HTTP_REQUEST_COUNT,end);
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
