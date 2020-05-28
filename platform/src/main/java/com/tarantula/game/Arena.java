package com.tarantula.game;

import com.tarantula.platform.OnApplicationHeader;

import java.util.Map;

public class Arena extends OnApplicationHeader {
    public int level;
    public double xp;
    public int capacity;
    public int duration; //minutes
    public int playMode; //0,1,2
    public Arena(){}
    public Arena(int level,double xp,String name,int capacity,int duration,int playMode){
        this.level = level;
        this.xp = xp;
        this.name = name;
        this.capacity = capacity;
        this.duration = duration;
        this.playMode = playMode;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("name",name);
        this.properties.put("level",level);
        this.properties.put("xp",xp);
        this.properties.put("capacity",capacity);
        this.properties.put("duration",duration);
        this.properties.put("playMode",playMode);
        this.properties.put("disabled",disabled);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name =(String)properties.get("name");
        this.level = ((Number)properties.get("level")).intValue();
        this.xp = ((Number)properties.get("xp")).doubleValue();
        this.capacity = ((Number)properties.get("capacity")).intValue();
        this.duration = ((Number)properties.get("duration")).intValue();
        this.playMode = ((Number)properties.get("playMode")).intValue();
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
