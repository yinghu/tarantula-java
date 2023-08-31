package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.CipherUtil;

import com.tarantula.platform.LobbyTypeIdIndex;
import com.tarantula.platform.service.PresenceKey;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;


public class DeployTestSet {

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
    @Test(groups = { "PresenceKey" })
    public void presenceKeyTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula");
        String bkey = CipherUtil.toBase64Key();
        byte[] key = CipherUtil.fromBase64Key(bkey);
        long id = dataStoreProvider.nextId(dataStore.name());
        PresenceKey presenceKey = new PresenceKey();
        presenceKey.base64key(bkey);
        presenceKey.id(id);
        Assert.assertTrue(dataStore.createIfAbsent(presenceKey,false));
        Assert.assertEquals(true, Arrays.equals(key,presenceKey.toKey()));
        PresenceKey load = new PresenceKey();
        load.id(id);
        Assert.assertFalse(dataStore.createIfAbsent(load,true));
        Assert.assertEquals(true, Arrays.equals(key,load.toKey()));
    }

    @Test(groups = { "lobbyTypeIdIndex" })
    public void lobbyTypeIdIndexTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula");
        String id = "testId";//dataStoreProvider.nextId(dataStore.name());
        LobbyTypeIdIndex created = new LobbyTypeIdIndex(id,"game",100,0);

        Assert.assertTrue(dataStore.createIfAbsent(created,false));
        Assert.assertEquals(created.lobbyId,100);
        LobbyTypeIdIndex load = new LobbyTypeIdIndex(id,"game");

        Assert.assertFalse(dataStore.createIfAbsent(load,true));
        Assert.assertEquals(load.lobbyId, 100);
        Assert.assertEquals(load.gameClusterId, 0);
    }
}
