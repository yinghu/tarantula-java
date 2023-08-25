package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.AccessIndexTrack;
import com.icodesoftware.service.DataStoreProvider;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AccessIndexTest {

    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }


    @Test(groups = { "AccessIndex" })
    public void accessIndexTest() {
        DataStore dataStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.AccessIndexStore.STORE_NAME_PREFIX+1);
        String access = "access100";
        AccessIndexTrack accessIndexTrack = new AccessIndexTrack(access,"BDS",SystemUtil.oid(), AccessIndex.USER_INDEX);
        accessIndexTrack.id(dataStoreProvider.nextId(dataStore.name()));
        Assert.assertTrue(accessIndexTrack.id()>0);
        Assert.assertTrue(dataStore.createIfAbsent(accessIndexTrack,false));
        AccessIndex load = new AccessIndexTrack(access);
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(accessIndexTrack.id(),load.id());
        Assert.assertTrue(dataStore.delete(accessIndexTrack));
        Assert.assertFalse(dataStore.load(accessIndexTrack));
    }

}
