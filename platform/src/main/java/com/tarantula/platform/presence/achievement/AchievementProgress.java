package com.tarantula.platform.presence.achievement;

import com.google.gson.JsonObject;
import com.icodesoftware.util.RecoverableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;


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
    public boolean write(DataBuffer buffer) {
        buffer.writeInt(tier);
        buffer.writeInt(target);
        buffer.writeDouble(objective);
        buffer.writeDouble(progress);
        buffer.writeBoolean(disabled);
        return true;
    }

    @Override
    public boolean read(DataBuffer buffer) {
        tier = buffer.readInt();
        target = buffer.readInt();
        objective = buffer.readDouble();
        progress = buffer.readDouble();
        disabled = buffer.readBoolean();
        return true;
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
