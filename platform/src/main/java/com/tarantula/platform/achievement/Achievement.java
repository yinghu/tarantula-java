package com.tarantula.platform.achievement;

import com.icodesoftware.Configurable;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class Achievement extends ConfigurableObject {


    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ACHIEVEMENT_CID;
    }
    public String name(){
        return header.get("name").getAsString();
    }
    public double goal(){
        return header.get("goal").getAsDouble();
    }
    @Override
    public  <T extends Configurable> T setup(){
        return (T)this;
    }
}
