package com.tarantula.platform.presence.saves;

import com.google.gson.JsonObject;
import com.icodesoftware.Configurable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.achievement.AchievementProgress;
import com.tarantula.platform.presence.DailyLoginTrack;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class SavedGame extends RecoverableObject implements Configurable {

    //index -- device id
    //version -- game latest update mark
    public DailyLoginTrack dailyLoginTrack;
    public AchievementProgress achievementProgress;
    public PlayerSaveIndex playerSaveIndex;

    public SavedGame(){

    }
    public SavedGame(String owner,String deviceId,String deviceName){
        this.owner = owner;
        this.index = deviceId;
        this.name = deviceName;
    }
    @Override
    public Map<String,Object> toMap(){
        this.properties.put("1",index);
        this.properties.put("2",name);
        this.properties.put("3",owner);
        this.properties.put("4",version);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = ((String) properties.get("1"));
        this.name = ((String) properties.get("2"));
        this.owner = ((String) properties.get("3"));
        this.version = ((Number)properties.getOrDefault("4",0)).intValue();
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
        jsonObject.addProperty("deviceName",name);
        jsonObject.addProperty("owner",owner);
        jsonObject.addProperty("version",version);
        if(dailyLoginTrack!=null) jsonObject.add("dailyLogin", dailyLoginTrack.toJson());
        if(achievementProgress!=null) jsonObject.add("achievement", achievementProgress.toJson());
        if(playerSaveIndex!=null) jsonObject.add("playerSaveIndex",playerSaveIndex.toJson());
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
        //if(this.dataStore==null) return;
        this.dataStore.update(this);
    }
}
