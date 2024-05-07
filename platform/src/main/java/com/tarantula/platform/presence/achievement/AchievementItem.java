package com.tarantula.platform.presence.achievement;

import com.tarantula.platform.item.*;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class AchievementItem extends Application{

    public AchievementItem(){}

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ACHIEVEMENT_CID;
    }

    public int tier(){
        return header.get("Tier").getAsInt();
    }
    public int target(){
        return header.get("Target").getAsInt();
    }
    @Override
    public String name(){
        return "tier_"+header.get("Tier").getAsInt()+"_target_"+header.get("Target").getAsInt();
    }
    public double objective(){
        return header.get("Objective").getAsDouble();
    }

    @Override
    public boolean configureAndValidate() {
        setup();
        return validated;
    }

}
