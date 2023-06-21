package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class SavedGame extends RecoverableObject implements Configurable {

    //index -- device id
    //version -- game latest update mark

    public int version;

    private boolean loaded;

    public SavedGame(){

    }
    public SavedGame(String owner,String saveName){
        this.owner = owner;
        this.name = saveName;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("2",name);
        this.properties.put("3",owner);
        this.properties.put("4",version);
        this.properties.put("5",timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = ((String) properties.get("2"));
        this.owner = ((String) properties.get("3"));
        this.version = ((Number)properties.getOrDefault("4",0)).intValue();
        this.timestamp = ((Number)properties.getOrDefault("5",0)).longValue();
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
        jsonObject.addProperty("GameId",this.distributionKey());
        jsonObject.addProperty("SaveName",name);
        jsonObject.addProperty("OwnerId",owner);
        jsonObject.addProperty("Version",version);
        jsonObject.addProperty("Timestamp",timestamp);
        return jsonObject;
    }

    public boolean onDevice(String systemId,String deviceId){
        return this.owner.equals(systemId) && this.index.equals(deviceId);
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

    @Override
    public void update(){
        this.dataStore.update(this);
    }

    public void load(){
        if(loaded) return;
        loaded = true;
        this.dataStore.load(this);
    }

}
