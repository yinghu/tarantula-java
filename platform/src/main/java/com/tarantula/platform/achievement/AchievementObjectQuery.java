package com.tarantula.platform.achievement;

import com.icodesoftware.RecoverableFactory;

import com.tarantula.platform.presence.PresencePortableRegistry;

public class AchievementObjectQuery implements RecoverableFactory<Achievement> {

    public String label;


    public AchievementObjectQuery(String query){
        this.label = query;
    }

    @Override
    public Achievement create() {
        return new Achievement();
    }

    @Override
    public int registryId() {
        return PresencePortableRegistry.ACHIEVEMENT_CID;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String distributionKey() {
        return null;
    }
}
