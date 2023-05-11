package com.tarantula.platform.resource;

import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

public class GameResource extends Application {

    public GameResource(ConfigurableObject configurableObject){
        super(configurableObject);
    }

    public GameResource(){

    }

    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.GAME_RESOURCE_CID;
    }

    @Override
    public String name(){
        return header.get("Name").getAsString();
    }

}
