package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class SavedGame extends RecoverableObject {

    //index -- device id
    //version -- game latest update mark
    public SavedGame(){

    }
    public SavedGame(String owner,String deviceId){
        this.owner = owner;
        this.index = deviceId;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",index);
        this.properties.put("2",owner);
        this.properties.put("3",version);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = ((String) properties.get("1"));
        this.owner = ((String) properties.get("2"));
        this.version = ((Number)properties.getOrDefault("3",0)).intValue();
    }

    @Override
    public int getClassId() {
        return PresencePortableRegistry.SAVED_GAME_CID;
    }

    @Override
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("gameId",this.distributionKey());
        jsonObject.addProperty("deviceId",index);
        jsonObject.addProperty("owner",owner);
        jsonObject.addProperty("version",version);
        return jsonObject;
    }
    @Override
    public boolean equals(Object obj){
        SavedGame savedGame =(SavedGame) obj;
        return savedGame.distributionKey().equals(this.distributionKey());
    }
    @Override
    public int hashCode(){
        return (distributionKey()).hashCode();
    }
}
