package com.tarantula.platform.presence;

import com.tarantula.platform.item.*;

public class DailyGiveaway extends Application {

    public DailyGiveaway(){

    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.DAILY_GIVEAWAY_CID;
    }

    public int tier(){
        return header.get("Tier").getAsInt();
    }
    public int day(){
        return header.get("Day").getAsInt();
    }

    @Override
    public String name(){
        return "t_"+tier()+"_d_"+day();
    }

    @Override
    public boolean configureAndValidate() {
        setup();
        return validated;
    }
}
