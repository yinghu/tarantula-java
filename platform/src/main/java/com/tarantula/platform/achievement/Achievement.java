package com.tarantula.platform.achievement;

import com.tarantula.platform.item.*;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class Achievement extends Application{

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
        //Application app = new Application(this);
        //app.dataStore(this.dataStore);
        //app.registerListener(this);
        setup();
        return validated;
    }

}
