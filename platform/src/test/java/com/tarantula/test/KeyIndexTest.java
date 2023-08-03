package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.icodesoftware.service.KeyIndexService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.room.GameEntry;
import com.tarantula.platform.room.GameEntryQuery;
import com.tarantula.platform.room.PVEGameRoom;
import com.tarantula.platform.room.PVPGameRoom;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.KeyIndexTrack;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class KeyIndexTest {

    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;

    DataStore dataStore;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
        dataStore = dataStoreProvider.createKeyIndexDataStore(KeyIndexService.KeyIndexStore.STORE_NAME_PREFIX+1);
    }

    @Test(groups = { "KeyIndex" })
    public void setupTest() {
        String masterNode = "n01";
        String slaveNode1 = "n02";
        String slaveNode2 = "n03";
        KeyIndexTrack index = new KeyIndexTrack();
        index.index("testkey");
        Assert.assertTrue(index.placeMasterNode(masterNode));
        Assert.assertFalse(index.placeMasterNode(masterNode));
        Assert.assertTrue(dataStore.createIfAbsent(index,false));
        Assert.assertFalse(dataStore.createIfAbsent(index,true));
        Assert.assertTrue(index.placeSlaveNode(slaveNode1));
        Assert.assertTrue(index.placeSlaveNode(slaveNode2));
        dataStore.update(index);
        KeyIndexTrack load = new KeyIndexTrack();
        load.index("testkey");
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(load.masterNode(),index.masterNode());
        Assert.assertEquals(load.slaveNodes().length,index.slaveNodes().length);
        Assert.assertEquals(load.slaveNodes()[0],slaveNode2);
    }



}
