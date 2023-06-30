package com.tarantula.platform.achievement;

import com.google.gson.JsonObject;
import com.icodesoftware.Recoverable;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.AssociateKey;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class AchievementProgress extends RecoverableObject {

    private int tier;//start 1
    private int target;//start 1
    private double progress;
    private double objective;


    public AchievementProgress(){
        this.label = "achievementProgress";
        this.tier = 1;
        this.disabled = true;
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
        properties.put("1",tier);
        properties.put("2",target);
        properties.put("3",objective);
        properties.put("4",progress);
        properties.put("5",disabled);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.tier = ((Number)properties.get("1")).intValue();
        this.target = ((Number)properties.get("2")).intValue();
        this.objective = ((Number)properties.get("3")).doubleValue();
        this.progress = ((Number)properties.get("4")).doubleValue();
        this.disabled = (boolean)properties.get("5");
    }
    public int tier(){
        return tier;
    }
    public int target(){
        return target;
    }
    public double progress(){
        return progress;
    }
    public double objective(){
        return objective;
    }
    public boolean onProgress(double delta){
        progress +=delta;
        if(progress>=objective){
            return true;
        }
        update();
        return false;
    }
    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Tier",tier);
        jsonObject.addProperty("Target",target);
        jsonObject.addProperty("Progress",progress);
        jsonObject.addProperty("Objective",objective);
        return jsonObject;
    }
    @Override
    public String name(){
        return "tier_"+tier+"_target_"+target;
    }
    @Override
    public Recoverable.Key key(){
        return new AssociateKey(this.bucket,this.oid,this.label);
    }
    public void reset(int tier,int target,double objective){
        this.tier = tier;
        this.target = target;
        this.objective = objective;
        this.progress = 0;
        this.disabled = false;
        update();
    }
    public void reset(){
        reset(1,0,0);
    }
}
