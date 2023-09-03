package com.tarantula.test;


import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.CipherUtil;

import com.icodesoftware.util.JWTUtil;
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
        byte[] tkey = JWTUtil.key();
        PresenceKey presenceKey = new PresenceKey();
        presenceKey.clusterKey(bkey);
        presenceKey.tokenKey(CipherUtil.toBase64Key(tkey));
        presenceKey.oid(serviceContext.node().nodeId());
        Assert.assertTrue(dataStore.createIfAbsent(presenceKey,false));
        Assert.assertEquals(true, Arrays.equals(key,presenceKey.clusterKey()));
        PresenceKey load = new PresenceKey();
        load.oid(serviceContext.node().nodeId());
        Assert.assertFalse(dataStore.createIfAbsent(load,true));
        Assert.assertEquals(Arrays.equals(key,load.clusterKey()),true);
        Assert.assertEquals(Arrays.equals(tkey,load.tokenKey()),true);
    }

    @Test(groups = { "lobbyTypeIdIndex" })
    public void lobbyTypeIdIndexTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula");
        LobbyTypeIdIndex created = new LobbyTypeIdIndex(serviceContext.node().bucketId(),"game","lobbyId","gameClusterId");

        Assert.assertTrue(dataStore.createIfAbsent(created,false));
        Assert.assertEquals(created.owner(),"lobbyId");
        LobbyTypeIdIndex load = new LobbyTypeIdIndex(serviceContext.node().bucketId(),"game");

        Assert.assertFalse(dataStore.createIfAbsent(load,true));
        Assert.assertEquals(load.owner(), "lobbyId");
        Assert.assertEquals(load.index(), "gameClusterId");
    }
}
