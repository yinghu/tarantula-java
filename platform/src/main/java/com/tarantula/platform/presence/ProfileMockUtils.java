package com.tarantula.platform.presence;

import java.util.Random;

public class ProfileMockUtils {
    public static Profile getRandomProfile(String playerID) {
        var profile = new Profile();
        profile.displayName = "Mock" + playerID;
        profile.iconIndex = new Random().nextInt(0, 4);
        return profile;
    }
}
