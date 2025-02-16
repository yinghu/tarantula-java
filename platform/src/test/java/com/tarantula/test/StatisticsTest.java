package com.tarantula.test;

import com.icodesoftware.DataStore;


import com.icodesoftware.protocol.statistics.TRStatistics;
import org.testng.Assert;

import org.testng.annotations.Test;


public class StatisticsTest extends DataStoreHook{


    @Test(groups = { "Statistics" })
    public void statisticsSetup(){
        DataStore ds = dataStoreProvider.createDataStore("test-statistics");
        TRStatistics userStatistics = new TRStatistics();
        userStatistics.distributionId(1000);
        userStatistics.dataStore(ds);
        userStatistics.load();
        userStatistics.entry("kills").update(1).update();
        userStatistics.entry("wins").update(1).update();

        TRStatistics load = new TRStatistics();
        load.distributionId(1000);
        load.dataStore(ds);
        load.load();
        Assert.assertEquals(load.entry("kills").total(),1);
        Assert.assertEquals(load.entry("wins").total(),1);

    }

}
