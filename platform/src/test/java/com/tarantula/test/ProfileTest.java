package com.tarantula.test;

import com.icodesoftware.DataStore;
import com.tarantula.platform.presence.Profile;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ProfileTest extends DataStoreHook{


    @Test(groups = { "Profile" })
    public void createProfileTest() {
        DataStore dataStore = dataStoreProvider.createDataStore("test_profile");
        long presenceId = serviceContext.distributionId();

        Profile profile = new Profile();

        profile.displayName = "Erik-Test";
        profile.iconIndex = 1;
        profile.distributionId(presenceId);

        Assert.assertTrue(dataStore.createIfAbsent(profile, false));

        Profile profileLoaded = new Profile();
        profileLoaded.distributionId(presenceId);

        Assert.assertTrue(dataStore.load(profileLoaded));

        Assert.assertEquals(profileLoaded.displayName, "Erik-Test");
        Assert.assertEquals(profileLoaded.iconIndex, 1);
    }
}
