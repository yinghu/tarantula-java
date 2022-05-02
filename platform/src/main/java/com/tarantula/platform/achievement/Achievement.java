package com.tarantula.platform.achievement;

import com.tarantula.platform.item.*;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class Achievement extends GrantableObject{

    public Achievement(){}

    public Achievement(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ACHIEVEMENT_CID;
    }
    public String name(){
        return header.get("Name").getAsString();
    }
    public double goal(){
        return header.get("Goal").getAsDouble();
    }

}
