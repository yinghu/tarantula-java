package com.tarantula.platform.achievement;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

import com.tarantula.platform.presence.PresencePortableRegistry;

public class AchievementObjectQuery implements RecoverableFactory<Achievement> {

    public String label;
    Recoverable.Key key;

    public AchievementObjectQuery(Recoverable.Key key,String query){
        this.key = key;
        this.label = query;
    }

    @Override
    public Achievement create() {
        return new Achievement();
    }

    @Override
    public String label() {
        return label;
    }
    @Override
    public Recoverable.Key key() {
        return key;
    }
}
