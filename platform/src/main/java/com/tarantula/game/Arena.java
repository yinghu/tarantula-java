package com.tarantula.game;

import com.tarantula.Module;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;

public class Arena extends RecoverableObject {

    public int level =1;
    public double xp =100;



    public synchronized Stub join(String systemId){
        Stub stub = new Stub();
        stub.seat = 2;
        stub.roomId = "roomid";
        return stub;
    }

    public synchronized void leave(String systemId,Stub stub){

    }

    public synchronized void onTimer(Module.OnUpdate update){

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
