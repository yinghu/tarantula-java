package com.tarantula.platform.presence;

import com.tarantula.platform.item.*;

public class DailyGiveaway extends GrantableObject {

    public DailyGiveaway(){}

    public DailyGiveaway(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.DAILY_GIVEAWAY_CID;
    }

    public int week(){
        return header.get("Week").getAsInt();
    }
    public int day(){
        return header.get("Day").getAsInt();
    }
    public String name(){
        return "w_"+week()+"_d_"+day();
    }
}
