package com.tarantula.game;

import com.tarantula.platform.RecoverableObject;

import java.util.Map;
import java.util.UUID;

public class Room extends RecoverableObject {

    public int capacity;
    private int totalJoined;
    private boolean dedicated;

    public Room(){
        this.oid = UUID.randomUUID().toString();
    }


    @Override
    public Map<String,Object> toMap(){
        this.properties.put("totalJoined",totalJoined);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.totalJoined =((Number)properties.get("totalJoined")).intValue();
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
