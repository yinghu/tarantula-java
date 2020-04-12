package com.tarantula.game;

import com.tarantula.platform.RecoverableObject;

import java.util.Map;

public class Room extends RecoverableObject {

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("oid","roomId");
        this.properties.put("totalJoined",3);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        //this.level =((Number)properties.get("level")).intValue();
        //this.xp = ((Number)properties.get("xp")).doubleValue();
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.ROOM_CID;
    }
}
