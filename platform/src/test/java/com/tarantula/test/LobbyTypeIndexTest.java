package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.tarantula.platform.LobbyTypeIdIndex;

import org.testng.Assert;
import org.testng.annotations.Test;


public class LobbyTypeIndexTest extends DataStoreHook{


    @Test(groups = { "lobbyTypeIdIndex" })
    public void lobbyTypeIdIndexTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula");
        long deploymentId = serviceContext.distributionId();
        long lobbyId = serviceContext.distributionId();
        long gameClusterId = serviceContext.distributionId();
        LobbyTypeIdIndex created = new LobbyTypeIdIndex(deploymentId,"holee-lobby",lobbyId,gameClusterId);
        Assert.assertTrue(dataStore.createIfAbsent(created,false));
        Assert.assertEquals(created.lobbyId(),lobbyId);
        Assert.assertEquals(created.gameClusterId(),gameClusterId);
        LobbyTypeIdIndex load = new LobbyTypeIdIndex(deploymentId,"holee-lobby");
        Assert.assertFalse(dataStore.createIfAbsent(load,true));
        Assert.assertEquals(load.lobbyId(),lobbyId);
        Assert.assertEquals(load.gameClusterId(),gameClusterId);
    }
}
