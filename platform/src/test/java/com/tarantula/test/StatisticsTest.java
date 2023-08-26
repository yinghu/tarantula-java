package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.Statistics;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.LongTypeKey;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.LobbyDescriptor;
import com.tarantula.platform.service.deployment.ApplicationQuery;
import com.tarantula.platform.service.deployment.LobbyQuery;
import com.tarantula.platform.service.deployment.XMLParser;
import com.tarantula.platform.statistics.StatisticsEntry;
import com.tarantula.platform.statistics.StatisticsEntryQuery;
import com.tarantula.platform.statistics.UserStatistics;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;


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
        userStatistics.id(1000);
        userStatistics.dataStore(ds);
        ds.createIfAbsent(userStatistics,true);
        userStatistics.load();
        Statistics.Entry kills = userStatistics.entry("kills");
        Statistics.Entry wins = userStatistics.entry("wins");
        Assert.assertTrue(kills.id()>0);
        Assert.assertTrue(wins.id()>0);
        UserStatistics load = new UserStatistics();
        load.id(1000);
        load.dataStore(ds);
        ds.createIfAbsent(load,true);
        load.load();
        Assert.assertEquals(load.count(0),2);
    }

}
