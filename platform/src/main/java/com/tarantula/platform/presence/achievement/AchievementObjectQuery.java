package com.tarantula.platform.presence.achievement;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class AchievementObjectQuery implements RecoverableFactory<AchievementItem> {

    public static final String label = "Achievement";
    Recoverable.Key key;

    public AchievementObjectQuery(Recoverable.Key key){
        this.key = key;
    }

    @Override
    public AchievementItem create() {
        return new AchievementItem();
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
