package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.Session;
import com.icodesoftware.util.RecoverableObject;
import com.icodesoftware.util.TimeUtil;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.time.LocalDateTime;
import java.util.Map;

public class SavedGame extends RecoverableObject implements Configurable {

    //index -- device id
    //version -- game latest update mark

    public int version;
    public int stub;

    private boolean loaded;

    public SavedGame(){

    }
    public SavedGame(String owner,String saveName){
        this.owner = owner;
        this.name = saveName;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",name);
        this.properties.put("2",owner);
        this.properties.put("3",version);
        this.properties.put("4",timestamp);
        this.properties.put("5",stub);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.name = ((String) properties.get("1"));
        this.owner = ((String) properties.get("2"));
        this.version = ((Number)properties.getOrDefault("3",0)).intValue();
        this.timestamp = ((Number)properties.getOrDefault("4",0)).longValue();
        this.stub = ((Number)properties.getOrDefault("5",0)).intValue();
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
        jsonObject.addProperty("Stub",stub);
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

    @Override
    public void update(){
        this.dataStore.update(this);
    }

    public void load(){
        if(loaded) return;
        loaded = true;
        this.dataStore.load(this);
    }
    public boolean onSession(Session session){
        if(stub==0){
            stub = session.stub();
            timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
            this.update();
            return true;
        }
        return stub == session.stub();
    }
    public void offSession(Session session){
        if(stub != session.stub()) return;
        stub = 0;
        timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.update();
    }

    public void expireSession(int expired){
        if(stub != expired) return;
        stub = 0;
        timestamp = TimeUtil.toUTCMilliseconds(LocalDateTime.now());
        this.update();
    }

}
