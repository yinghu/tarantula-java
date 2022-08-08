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

    public int version;

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
        this.properties.put("5",timestamp);
        return this.properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = ((String) properties.get("1"));
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
        jsonObject.addProperty("DeviceId",index);
        jsonObject.addProperty("DeviceName",name);
        jsonObject.addProperty("OwnerId",owner);
        jsonObject.addProperty("Version",version);
        jsonObject.addProperty("Timestamp",timestamp);
        if(dailyLoginTrack!=null) jsonObject.add("_dailyLogin", dailyLoginTrack.toJson());
        if(achievementProgress!=null) jsonObject.add("_achievement", achievementProgress.toJson());
        if(playerSaveIndex!=null) jsonObject.add("_playerSaveIndex",playerSaveIndex.toJson());
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
