package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.tournament.PlayerTournamentHistory;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import java.util.List;

public class UserTest {

    DataStoreProvider dataStoreProvider;
    ServiceContext serviceContext;
    @BeforeClass
    public void setUp() {
        DataStoreTestEvn.setUp();
        dataStoreProvider = DataStoreTestEvn.dataStoreProvider;
        serviceContext = DataStoreTestEvn.serviceContext;
    }


    //@Test(groups = { "DataStore" })
    public void userTest() {
        DataStore accessStore = dataStoreProvider.createAccessIndexDataStore("test_access_index");
        AccessIndex accessIndex = new AccessIndexTrack("test1","BDS", SystemUtil.oid(),1);
        accessIndex.id(dataStoreProvider.nextId(accessStore.name()));
        Assert.assertTrue(accessStore.createIfAbsent(accessIndex,false));
        DataStore dUser = dataStoreProvider.create("test_user",serviceContext.node().partitionNumber());
        DataStore dPresence = dataStoreProvider.create("test_presence",serviceContext.node().partitionNumber());
        DataStore dAccount = dataStoreProvider.create("test_account",serviceContext.node().partitionNumber());
        User user = new User();
    }

}
