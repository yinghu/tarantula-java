package com.tarantula.platform.presence.dailygiveaway;

import com.icodesoftware.RecoverableFactory;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class DailygGiveawayObjectQuery implements RecoverableFactory<DailyGiveaway> {

    public String label;


    public DailygGiveawayObjectQuery(String query){
        this.label = query;
    }

    @Override
    public DailyGiveaway create() {
        return new DailyGiveaway();
    }

    @Override
    public int registryId() {
        return PresencePortableRegistry.DAILY_GIVEAWAY_CID;
    }

    @Override
    public String label() {
        return label;
    }


    public String distributionKey() {
        return null;
    }
}
