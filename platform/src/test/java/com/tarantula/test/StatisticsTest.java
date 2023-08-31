package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Statistics;

import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;

import com.tarantula.platform.statistics.UserStatistics;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.UUID;


public class StatisticsTest {


    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }


    @AfterTest
    public void tearDown() throws Exception{
       dataStoreProvider.shutdown();
    }


    @Test(groups = { "Statistics" })
    public void statisticsHook(){
        DataStore ds = dataStoreProvider.createDataStore("statistics");
        UserStatistics userStatistics = new UserStatistics();
        userStatistics.oid(UUID.randomUUID().toString());
        userStatistics.dataStore(ds);
        ds.createIfAbsent(userStatistics,true);
        userStatistics.load();
        Statistics.Entry kills = userStatistics.entry("kills");
        Statistics.Entry wins = userStatistics.entry("wins");
        ///Assert.assertTrue(kills.id()>0);
        //Assert.assertTrue(wins.id()>0);
        UserStatistics load = new UserStatistics();
        load.oid(userStatistics.oid());
        load.dataStore(ds);
        ds.createIfAbsent(load,true);
        load.load();
        Assert.assertEquals(load.count(0),2);
    }

}
