package com.tarantula.test;

import com.icodesoftware.AccessIndex;
import com.icodesoftware.DataStore;
import com.icodesoftware.service.AccessIndexService;
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
        DataStore accessStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME);
        AccessIndex accessIndex = new AccessIndexTrack("test1","BDS", SystemUtil.oid(),1);
        accessIndex.id(dataStoreProvider.nextId(accessStore.name()));
        Assert.assertTrue(accessStore.createIfAbsent(accessIndex,false));
        DataStore dUser = dataStoreProvider.createDataStore("test_user");
        DataStore dPresence = dataStoreProvider.createDataStore("test_presence");
        DataStore dAccount = dataStoreProvider.createDataStore("test_account");
        User user = new User();
    }

}
