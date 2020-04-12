package com.tarantula.game;

import com.tarantula.platform.RecoverableObject;

import java.util.Map;

public class Arena extends RecoverableObject {

    public int level =1;
    public double xp =100;

    public Room join(String systemId){
        return new Room();
    }

    @Override
    public Map<String,Object> toMap(){
        this.properties.put("level",level);
        this.properties.put("xp",xp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.level =((Number)properties.get("level")).intValue();
        this.xp = ((Number)properties.get("xp")).doubleValue();
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.ARENA_CID;
    }

}
