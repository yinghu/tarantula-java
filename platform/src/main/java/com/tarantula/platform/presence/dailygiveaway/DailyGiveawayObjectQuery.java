package com.tarantula.platform.presence.dailygiveaway;

import com.icodesoftware.Recoverable;
import com.icodesoftware.RecoverableFactory;

public class DailyGiveawayObjectQuery implements RecoverableFactory<DailyGiveaway> {

    public static final String label = "DailyGiveaway";
    private Recoverable.Key key;


    public DailyGiveawayObjectQuery(Recoverable.Key key){
        this.key = key;
    }

    @Override
    public DailyGiveaway create() {
        return new DailyGiveaway();
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
