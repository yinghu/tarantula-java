package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.Statistics;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.LongTypeKey;
import com.tarantula.admin.GameClusterQuery;
import com.tarantula.platform.GameCluster;
import com.tarantula.platform.resource.GameResourceQuery;
import com.tarantula.platform.statistics.UserStatistics;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class GameClusterTest {


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


    @Test(groups = { "GameCluster" })
    public void createGameCluster(){
        DataStore ds = dataStoreProvider.createDataStore("test_tarantula");
        GameCluster gameCluster = new GameCluster();
        gameCluster.name("beam");
        gameCluster.mode = "pve";
        gameCluster.developerIcon = "dicon";
        gameCluster.gameIcon = "gaicon";
        gameCluster.developer = "gdds";
        gameCluster.gameLobbyName = "beam/lobby";
        gameCluster.gameServiceName = "beam/service";
        gameCluster.gameDataName = "beam/data";
        gameCluster.tournamentEnabled = true;
        gameCluster.dedicated = false;
        gameCluster.applicationSetup = "com.tarantula.Appsetup";
        gameCluster.maxArenaCount = 10;
        gameCluster.maxZoneCount = 10;
        gameCluster.maxLobbyCount = 10;
        Assert.assertTrue(ds.create(gameCluster));

        GameCluster load = new GameCluster();
        load.id(gameCluster.id());
        Assert.assertTrue(ds.load(load));
        Assert.assertEquals(load.name(),gameCluster.name());
        gameCluster.ownerKey(new LongTypeKey(1000));
        gameCluster.onEdge(true);
        Assert.assertTrue(ds.createEdge(gameCluster,"gameCluster"));
        gameCluster.ownerKey(new LongTypeKey(2000));
        Assert.assertTrue(ds.createEdge(gameCluster,"gameCluster"));
        Assert.assertEquals(ds.list(new GameClusterQuery(1000)).size(),1);
        Assert.assertEquals(ds.list(new GameClusterQuery(2000)).size(),1);

    }

}
