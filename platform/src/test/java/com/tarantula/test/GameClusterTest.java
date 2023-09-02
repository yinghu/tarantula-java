package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.OidKey;
import com.tarantula.admin.GameClusterQuery;
import com.tarantula.platform.GameCluster;
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
        String accountId = "accoutId";
        String publishingId = "publishingId";
        GameCluster gameCluster = new GameCluster();
        gameCluster.name("beam");
        gameCluster.mode = "pve";
        gameCluster.developerIcon = "dicon";
        gameCluster.gameIcon = "gaicon";
        gameCluster.developer = "gdds";
        //gameCluster.gameLobbyName = "beam/lobby";
        //gameCluster.gameServiceName = "beam/service";
        //gameCluster.gameDataName = "beam/data";
        gameCluster.tournamentEnabled = true;
        gameCluster.dedicated = false;
        gameCluster.applicationSetup = "com.tarantula.Appsetup";
        gameCluster.maxArenaCount = 10;
        gameCluster.maxZoneCount = 10;
        gameCluster.maxLobbyCount = 10;
        gameCluster.maxDataSize = 4000;
        gameCluster.upgradeVersion = 1;
        Assert.assertTrue(ds.create(gameCluster));
        Assert.assertNull(gameCluster.gameLobbyName);
        gameCluster.gameLobbyName = "beam/lobby";
        Assert.assertTrue(ds.update(gameCluster));

        GameCluster load = new GameCluster();
        load.oid(gameCluster.oid());
        Assert.assertTrue(ds.load(load));
        Assert.assertEquals(load.name(),gameCluster.name());
        Assert.assertNotNull(load.gameLobbyName);

        gameCluster.ownerKey(new OidKey(accountId));
        gameCluster.onEdge(true);
        Assert.assertTrue(ds.createEdge(gameCluster,"gameCluster"));
        gameCluster.ownerKey(new OidKey(publishingId));
        Assert.assertTrue(ds.createEdge(gameCluster,"gameCluster"));
        Assert.assertEquals(ds.list(new GameClusterQuery(accountId)).size(),1);
        Assert.assertEquals(ds.list(new GameClusterQuery(publishingId)).size(),1);

    }

}
