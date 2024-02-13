package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Statistics;


import com.icodesoftware.protocol.statistics.UserStatistics;
import org.testng.Assert;

import org.testng.annotations.Test;


public class StatisticsTest extends DataStoreHook{


    @Test(groups = { "Statistics" })
    public void statisticsHook(){
        DataStore ds = dataStoreProvider.createDataStore("test-statistics");
        UserStatistics userStatistics = new UserStatistics();
        userStatistics.distributionId(serviceContext.distributionId());
        userStatistics.dataStore(ds);
        userStatistics.load();
        Statistics.Entry kills = userStatistics.entry("kills");
        Statistics.Entry wins = userStatistics.entry("wins");
        Assert.assertTrue(kills.distributionId()>0);
        Assert.assertTrue(wins.distributionId()>0);
        UserStatistics load = new UserStatistics();
        load.distributionId(userStatistics.distributionId());
        //Assert.assertFalse(ds.createIfAbsent(load,true));
        load.dataStore(ds);
        load.load();
        //Assert.assertEquals(load.count(0),2);
    }

}
