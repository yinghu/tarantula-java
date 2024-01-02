package com.tarantula.platform.achievement;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class AchievementObjectQuery implements RecoverableFactory<AchievementItem> {

    public String label;
    Recoverable.Key key;

    public AchievementObjectQuery(Recoverable.Key key,String query){
        this.key = key;
        this.label = query;
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
