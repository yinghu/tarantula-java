package com.tarantula.test;


import com.icodesoftware.DataStore;

import com.tarantula.platform.OnSessionQuery;
import com.tarantula.platform.PresenceIndex;

import com.tarantula.platform.SessionIndex;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;

public class PresenceIndexTest extends DataStoreHook{


    @Test(groups = { "PresenceIndex" })
    public void presenceKeyTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_presence");
        long presenceId = serviceContext.distributionId();
        PresenceIndex presenceIndex = new PresenceIndex();
        presenceIndex.distributionId(presenceId);
        Assert.assertTrue(dataStore.createIfAbsent(presenceIndex,true));
        SessionIndex t1 = new SessionIndex();
        t1.ownerKey(presenceIndex.key());
        Assert.assertTrue(dataStore.create(t1));
        SessionIndex t2 = new SessionIndex();
        t2.ownerKey(presenceIndex.key());
        Assert.assertTrue(dataStore.create(t2));
        List<SessionIndex> tlist = dataStore.list(new OnSessionQuery(presenceIndex.key()));
        Assert.assertEquals(tlist.size(),2);
    }

}
