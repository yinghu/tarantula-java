package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.service.AccessKey;
import com.tarantula.platform.service.AccessKeyQuery;
import com.tarantula.platform.service.DataStoreProvider;
import com.tarantula.platform.service.persistence.RevisionObject;
import com.tarantula.platform.tournament.PlayerTournamentHistory;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

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
        AccessIndexTrack accessIndexTrack = new AccessIndexTrack("access100","BDS",SystemUtil.oid(), AccessIndex.USER_INDEX);
        Assert.assertTrue(accessIndexTrack.key().asString().equals("access100"));
        Assert.assertTrue(dataStore.createIfAbsent(accessIndexTrack,false));

        //RevisionObject revisionObject = RevisionObject.fromBinary(dataStore.backup().get(accessIndexTrack.key().asString().getBytes()));
        //Assert.assertEquals(revisionObject.node,serviceContext.node().nodeName().getBytes());
        Assert.assertTrue(dataStore.delete(accessIndexTrack.key().asString().getBytes()));
        Assert.assertFalse(dataStore.load(accessIndexTrack));
    }

}
