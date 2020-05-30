package com.tarantula.game;

import com.tarantula.platform.OnApplicationHeader;

import java.util.Map;

public class Arena extends OnApplicationHeader {
    public int level;
    public double xp;

    public Arena(){}
    public Arena(int level,double xp,String name,boolean disabled){
        this.level = level;
        this.xp = xp;
        this.name = name;
        this.disabled = disabled;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("name",name);
        this.properties.put("level",level);
        this.properties.put("xp",xp);
        this.properties.put("disabled",disabled);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name =(String)properties.get("name");
        this.level = ((Number)properties.get("level")).intValue();
        this.xp = ((Number)properties.get("xp")).doubleValue();
        this.disabled = (boolean)properties.get("disabled");
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
