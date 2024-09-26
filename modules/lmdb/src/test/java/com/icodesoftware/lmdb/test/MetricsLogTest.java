package com.icodesoftware.lmdb.test;

import com.icodesoftware.Statistics;
import com.icodesoftware.lmdb.MetricsLog;
import com.icodesoftware.protocol.statistics.StatisticsEntry;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class MetricsLogTest {



    @Test(groups = { "metricsLog" })
    public void metricsTest(){
        List<Statistics.Entry> updates = List.of(StatisticsEntry.simpleValue("kills",100),StatisticsEntry.simpleValue("wins",200));
        MetricsLog metricsLog = MetricsLog.metricsLog("n01","metrics",updates);
        byte[] ret = metricsLog.toBinary();
        MetricsLog data = new MetricsLog();
        data.fromBinary(ret);
        Assert.assertEquals(metricsLog.node,data.node);
        Assert.assertEquals(metricsLog.name(),data.name());
        Assert.assertEquals(metricsLog.updates.size(),data.updates.size());
        int[] ct ={0};
        data.updates.forEach((e)->{
            if(e.name().equals("kills")){
                ct[0]++;
                Assert.assertEquals(e.total(),100);
            }
            if(e.name().equals("wins")){
                ct[0]++;
                Assert.assertEquals(e.total(),200);
            }
        });
        Assert.assertEquals(ct[0],2);
    }

}
