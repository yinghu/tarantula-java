package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.icodesoftware.util.CipherUtil;
import com.icodesoftware.util.JWTUtil;

import com.tarantula.platform.service.PresenceKey;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;


public class PresenceKeyTest extends DataStoreHook{


    @Test(groups = { "PresenceKey" })
    public void presenceKeyTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_tarantula");
        long nodeId = serviceContext.distributionId();
        String bkey = CipherUtil.toBase64Key();
        byte[] key = CipherUtil.fromBase64Key(bkey);
        byte[] tkey = JWTUtil.key();
        PresenceKey presenceKey = new PresenceKey(nodeId);
        presenceKey.clusterKey(bkey);
        presenceKey.tokenKey(CipherUtil.toBase64Key(tkey));
        Assert.assertTrue(dataStore.createIfAbsent(presenceKey,false));
        Assert.assertTrue(Arrays.equals(key,presenceKey.clusterKey()));
        Assert.assertTrue(Arrays.equals(tkey,presenceKey.tokenKey()));
        PresenceKey load = new PresenceKey(nodeId);
        Assert.assertFalse(dataStore.createIfAbsent(load,true));
        Assert.assertEquals(Arrays.equals(key,load.clusterKey()),true);
        Assert.assertEquals(Arrays.equals(tkey,load.tokenKey()),true);
    }

}
