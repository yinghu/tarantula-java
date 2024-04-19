package com.tarantula.test;

import com.google.gson.JsonObject;
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
        JsonObject payload = new JsonObject();
        //cover configureAndValidate
        Assert.assertFalse(profile.configureAndValidate(payload.toString().getBytes()));

        payload.addProperty("DisplayName","Erik-Test");
        Assert.assertFalse(profile.configureAndValidate(payload.toString().getBytes()));

        payload.addProperty("IconIndex",1);
        Assert.assertTrue(profile.configureAndValidate(payload.toString().getBytes()));

        profile.distributionId(presenceId);

        Assert.assertTrue(dataStore.createIfAbsent(profile, false));

        Profile profileLoaded = new Profile();
        profileLoaded.distributionId(presenceId);

        Assert.assertTrue(dataStore.load(profileLoaded));

        Assert.assertEquals(profileLoaded.displayName, "Erik-Test");
        Assert.assertEquals(profileLoaded.iconIndex, 1);

        payload.addProperty("ProfileSequence",0);
        payload.addProperty("SystemId",profileLoaded.distributionId());

        //cover toJson
        Assert.assertEquals(profileLoaded.toJson().toString(), payload.toString());
    }
}
