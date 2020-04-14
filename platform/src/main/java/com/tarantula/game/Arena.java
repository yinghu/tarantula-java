package com.tarantula.game;

import com.tarantula.Module;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;

/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Arena extends RecoverableObject {

    public int level =1;
    public double xp =100;

    private Room[] rooms = new Room[]{new Room()};



    public synchronized Stub join(String systemId){
       return rooms[0].stub(systemId);
    }

    public synchronized void leave(String systemId,Stub stub){

    }

    public synchronized void onTimer(Module.OnUpdate update){
        rooms[0].onTimer(update);
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
