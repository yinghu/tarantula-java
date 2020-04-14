package com.tarantula.game;

import com.tarantula.Module;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;
import java.util.UUID;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Room extends RecoverableObject {

    public int capacity;
    private int totalJoined;
    private boolean dedicated;
    private long duration = 60*1000;

    public Room(){
        this.oid = UUID.randomUUID().toString();
    }
    public Stub stub(String systemId){
        Stub stub = new Stub();
        stub.seat = 1;
        stub.roomId = oid;
        totalJoined++;
        return stub;
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
    public void onTimer(Module.OnUpdate update){
        if(totalJoined==0){
            return;
        }
        duration = duration-1000;
        if(duration<=0){
            update.on(oid+"?onLeave",null);
            totalJoined=0;
            return;
        }
        update.on(oid+"?onTimer",new Countdown(duration).toJson().toString().getBytes());
    }
}
