package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Stub extends RecoverableObject {

    public int rank;
    public String roomId;
    public int seat;
    public String tag;


    public Stub(){}
    public Stub(int seat,String roomId){
        this.seat = seat;
        this.roomId = roomId;
    }
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("rank",rank);
        jo.addProperty("owner",owner);
        jo.addProperty("seat",seat);
        jo.addProperty("roomId",roomId);
        jo.addProperty("tag",tag);
        return jo;
    }

    @Override
    public Map<String,Object> toMap(){
        //this.properties.put("totalJoined",totalJoined);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        //this.totalJoined =((Number)properties.get("totalJoined")).intValue();
        //this.xp = ((Number)properties.get("xp")).doubleValue();
    }
    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.STUB_CID;
    }
}
