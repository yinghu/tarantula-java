package com.tarantula.test;

import com.icodesoftware.*;
import com.icodesoftware.service.AccessIndexService;
import com.icodesoftware.service.DataStoreProvider;
import com.icodesoftware.service.ServiceContext;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.AccessIndexTrack;
import com.tarantula.platform.PresenceIndex;
import com.tarantula.platform.presence.Membership;
import com.tarantula.platform.presence.User;
import com.tarantula.platform.presence.UserAccount;
import com.tarantula.platform.util.SystemUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;


public class UserTest extends DataStoreHook{


    @Test(groups = { "User" })
    public void userTest() {
        DataStore accessStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME);
        AccessIndex accessIndex = new AccessIndexTrack("test1",1,serviceContext.distributionId());
        Assert.assertTrue(accessStore.createIfAbsent(accessIndex,false));
        DataStore dUser = dataStoreProvider.createDataStore("test_user");
        DataStore pUser = dataStoreProvider.createDataStore("test_presence");
        User user = new User("user1",true, OnAccess.GAME_CENTER);
        user.password("password");
        user.emailAddress("email");
        user.role("root");
        user.distributionId(accessIndex.distributionId());
        Assert.assertTrue(dUser.createIfAbsent(user,false));
        User load = new User();
        load.distributionId(accessIndex.distributionId());
        Assert.assertTrue(dUser.load(load));
        Assert.assertEquals(load.login(),user.login());
        Assert.assertTrue(user.validate());
        user.primary(true);
        Assert.assertFalse(user.validate());
        user.primaryId(1);
        Assert.assertTrue(user.validate());

        PresenceIndex presenceIndex = new PresenceIndex();
        presenceIndex.distributionId(user.distributionId());
        Assert.assertTrue(pUser.createIfAbsent(presenceIndex,false));
    }

    //@Test(groups = { "Account" })
    public void accountTest() {
        DataStore accessStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME);
        AccessIndex accessIndex = new AccessIndexTrack("test11",1,0);
        //accessIndex.id(dataStoreProvider.nextId(accessStore.name()));
        Assert.assertTrue(accessStore.createIfAbsent(accessIndex,false));
        //DataStore dUser = dataStoreProvider.createDataStore("test_user");
        //DataStore dPresence = dataStoreProvider.createDataStore("test_presence");
        DataStore dAccount = dataStoreProvider.createDataStore("test_account");
        UserAccount user = new UserAccount();
        //user.oid(accessIndex.oid());
        user.userCount(1);
        user.gameClusterCount(1);
        Assert.assertTrue(dAccount.createIfAbsent(user,false));

        UserAccount load = new UserAccount();
        //load.oid(user.oid());
        Assert.assertTrue(dAccount.load(load));
        Assert.assertEquals(load.gameClusterCount(0),1);
        Assert.assertEquals(load.userCount(0),1);
    }
    //@Test(groups = { "Account" })
    public void presenceTest() {
        DataStore accessStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME);
        AccessIndex accessIndex = new AccessIndexTrack("test111",1,0);
        //accessIndex.id(dataStoreProvider.nextId(accessStore.name()));
        Assert.assertTrue(accessStore.createIfAbsent(accessIndex,false));
        //DataStore dUser = dataStoreProvider.createDataStore("test_user");
        DataStore dPresence = dataStoreProvider.createDataStore("test_presence");
        //DataStore dAccount = dataStoreProvider.createDataStore("test_account");
        Presence user = new PresenceIndex();
        //user.oid(accessIndex.oid());
        user.dataStore(dPresence);
        Assert.assertTrue(dPresence.createIfAbsent(user,false));
        //Assert.assertEquals(user.count(1),1);
        Presence load = new PresenceIndex();
        //load.oid(user.oid());
        Assert.assertFalse(dPresence.createIfAbsent(load,true));
        //Assert.assertEquals(load.count(0),1);
    }

    //@Test(groups = { "Membership" })
    public void membershipTest() {
        DataStore accessStore = dataStoreProvider.createAccessIndexDataStore(AccessIndexService.NAME);
        AccessIndex accessIndex = new AccessIndexTrack("test9111",1,0);
        //accessIndex.id(dataStoreProvider.nextId(accessStore.name()));
        Assert.assertTrue(accessStore.createIfAbsent(accessIndex,false));
        //DataStore dUser = dataStoreProvider.createDataStore("test_user");
        DataStore dSubscription = dataStoreProvider.createDataStore("test_subscription");
        //DataStore dAccount = dataStoreProvider.createDataStore("test_account");
        Subscription user = new Membership();
        //user.oid(accessIndex.oid());
        user.count(1);
        user.startTimestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now()));
        user.endTimestamp(TimeUtil.toUTCMilliseconds(LocalDateTime.now().plusMonths(1)));
        Assert.assertTrue(dSubscription.createIfAbsent(user,false));
        //Assert.assertEquals(user.count(1),1);
        Subscription load = new Membership();
        //load.oid(user.oid());
        Assert.assertFalse(dSubscription.createIfAbsent(load,true));
        Assert.assertEquals(load.count(0),1);
    }
}
