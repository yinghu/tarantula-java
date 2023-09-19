package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.KeyIndexService;

import com.icodesoftware.util.NaturalKey;

import com.tarantula.platform.service.KeyIndexTrack;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KeyIndexTest extends DataStoreHook{



    @Test(groups = { "KeyIndex" })
    public void setupTest() {
        DataStore dataStore = dataStoreProvider.createAccessIndexDataStore(KeyIndexService.KeyIndexStore.STORE_NAME+"_user");
        String masterNode = "n01";
        String slaveNode1 = "n02";
        String slaveNode2 = "n03";
        KeyIndexTrack index = new KeyIndexTrack("users",new NaturalKey("a100"));
        Assert.assertTrue(index.placeMasterNode(masterNode));
        Assert.assertFalse(index.placeMasterNode(masterNode));
        Assert.assertTrue(index.placeSlaveNode(slaveNode1));
        Assert.assertTrue(index.placeSlaveNode(slaveNode2));
        Assert.assertTrue(dataStore.createIfAbsent(index,false));
        Assert.assertFalse(dataStore.createIfAbsent(index,true));

        KeyIndexTrack index1 = new KeyIndexTrack("accesses",new NaturalKey("noop"));
        Assert.assertTrue(index1.placeMasterNode(masterNode));
        Assert.assertFalse(index1.placeMasterNode(masterNode));
        Assert.assertTrue(dataStore.createIfAbsent(index1,false));
        Assert.assertFalse(dataStore.createIfAbsent(index1,true));

        KeyIndexTrack load = new KeyIndexTrack("users",new NaturalKey("a100"));
        KeyIndexTrack load1 = new KeyIndexTrack("accesses",new NaturalKey("noop"));
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(index.masterNode(),load.masterNode());
        Assert.assertTrue(dataStore.load(load1));
        Assert.assertEquals(index1.masterNode(),load1.masterNode());


    }



}
