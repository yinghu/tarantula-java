package com.tarantula.game;

import com.google.gson.JsonObject;
import com.tarantula.platform.RecoverableObject;

import java.util.Map;
/**
 * Created by yinghu lu on 4/14/2020.
 */
public class Stub extends RecoverableObject {

    public String roomId;
    public int seat;
    public String tag;

    public int rank = 2; //rank of game 1 basis
    public double pxp = 30; //percentage of game xp 100 basis

    /**
     * pxp - performance xp percentage on 100 base points pxp*(100) 0.7*100 = 70 0.3*100 = 30
     * rank - final result 1,2 rank xp = (1/rank)*100  1 - 100 2 50 ..
     * xp-delta = (1/rank)*(100)+pxp*(100)+csw*(100); //cws only if last is cws
     * zxp = zxp +xp-delta
     * xp = xp + xp-delta
      */

    public Stub(){}
    public Stub(int seat,String roomId){
        this.seat = seat;
        this.roomId = roomId;
    }
    public JsonObject toJson(){
        JsonObject jo = new JsonObject();
        jo.addProperty("owner",owner);
        jo.addProperty("seat",seat);
        jo.addProperty("roomId",roomId);
        jo.addProperty("tag",tag);
        //jo.addProperty("rank",rank);
        //jo.addProperty("pxp",pxp);
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
