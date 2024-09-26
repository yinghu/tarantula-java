package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;

import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.util.NaturalKey;
import com.tarantula.platform.AccessIndexTrack;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AccessIndexTest extends DataStoreHook{


    @Test(groups = { "AccessIndex" })
    public void accessIndexTest() {
        DataStore dataStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.STORE_NAME);
        //Assert.assertEquals(dataStore.name(),AccessIndexService.AccessIndexStore.STORE_NAME);
        String access = "access100";
        AccessIndexTrack accessIndexTrack = new AccessIndexTrack(access, AccessIndex.USER_INDEX,serviceContext.distributionId());
        Assert.assertTrue(accessIndexTrack.validate());
        Assert.assertNotNull(accessIndexTrack.owner());
        Assert.assertTrue(accessIndexTrack.distributionId()>0);
        Assert.assertTrue(dataStore.createIfAbsent(accessIndexTrack,false));
        AccessIndex load = new AccessIndexTrack(access);
        Assert.assertTrue(dataStore.load(load));
        Assert.assertEquals(accessIndexTrack.referenceId(),load.referenceId());
        Assert.assertEquals(accessIndexTrack.distributionId(),load.distributionId());
        Assert.assertTrue(dataStore.delete(accessIndexTrack));
        Assert.assertFalse(dataStore.load(accessIndexTrack));
        Assert.assertTrue(accessIndexTrack.key().equals(new NaturalKey(access)));
    }


}
