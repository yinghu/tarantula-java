package com.tarantula.platform.achievement;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class AchievementProgress extends RecoverableObject {

    private double progress;
    private double objective;
    public AchievementProgress(){

    }
    public AchievementProgress(Achievement achievement){
        this.label = achievement.name();
        this.objective = achievement.objective();
    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.ACHIEVEMENT_PROGRESS_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("1",progress);
        properties.put("2",objective);
        properties.put("3",disabled);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.progress = ((Number)properties.get("1")).doubleValue();
        this.objective = ((Number)properties.get("2")).doubleValue();
        this.disabled = (boolean)properties.getOrDefault("3",false);
    }
    public double progress(){
        return progress;
    }
    public double objective(){
        return objective;
    }
    public boolean onProgress(double delta){
        progress +=delta;
        if(progress>=objective&&!disabled) return true;
        return false;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name",label);
        jsonObject.addProperty("progress",progress);
        jsonObject.addProperty("objective",objective);
        jsonObject.addProperty("passed",disabled);
        return jsonObject;
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
}
