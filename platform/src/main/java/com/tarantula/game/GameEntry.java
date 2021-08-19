package com.tarantula.game;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.RecoverableObject;

import java.util.Map;

public class GameEntry extends RecoverableObject implements Configurable {
    public static final String LABEL = "GGE";
    public String systemId;
    private int seat;
    public GameEntry(){
        this.label = LABEL;
        this.onEdge = true;
    }
    public GameEntry(int seat){
        this();
        this.seat = seat;
    }
    @Override
    public Map<String,Object> toMap(){
        properties.put("seat",seat);
        properties.put("systemId",systemId);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.seat  =  ((Number)properties.getOrDefault("seat",0)).intValue();
        this.systemId = (String)properties.getOrDefault("systemId",null);
    }

    @Override
    public int getFactoryId() {
        return GamePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return GamePortableRegistry.GAME_ENTRY_CID;
    }

    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("systemId",systemId);
        jsonObject.addProperty("seat",seat);
        return jsonObject;
    }

    @Override
    public boolean equals(Object objc){
        return systemId.equals(((GameEntry)objc).systemId);
    }
    public int hashCode(){
        return systemId.hashCode();
    }
}
