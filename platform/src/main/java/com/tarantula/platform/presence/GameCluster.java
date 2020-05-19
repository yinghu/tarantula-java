package com.tarantula.platform.presence;

import com.tarantula.platform.OnAccessTrack;
import com.tarantula.platform.event.PortableEventRegistry;


import java.util.Map;

public class GameCluster extends OnAccessTrack{

    @Override
    public Map<String,Object> toMap(){
        properties.put("2",name);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        name = (String) properties.get("2");
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.GAME_CLUSTER_CID;
    }
    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
}
