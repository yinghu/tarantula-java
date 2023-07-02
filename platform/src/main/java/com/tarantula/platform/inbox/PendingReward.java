package com.tarantula.platform.inbox;

import com.google.gson.JsonObject;
import com.tarantula.platform.item.Application;
import com.tarantula.platform.item.ConfigurableObject;
import com.tarantula.platform.presence.PresencePortableRegistry;

import java.util.Map;

public class PendingReward extends Application {


    //name -- the name of reward like daily giveaway , achievement , tournament
    //index -- this key of the reward
    //disabled -- rewarded already
    public PendingReward(){

    }
    public PendingReward(Application application){
        this.index = application.distributionKey();
        this.configurationName = application.configurationName();
        this.configurationTypeId = application.configurationTypeId();
        this.configurationCategory = application.configurationCategory();
        this.configurationType = application.configurationType();
    }
    public int getFactoryId() {
        return PresencePortableRegistry.OID;
    }
    @Override
    public int getClassId() {
        return PresencePortableRegistry.PENDING_REWARD_CID;
    }

    @Override
    public Map<String,Object> toMap(){
        properties.put("1",index);
        properties.put("2",configurationName);
        properties.put("3",configurationType);
        properties.put("4",configurationTypeId);
        properties.put("5",configurationCategory);
        properties.put("6",disabled);
        return properties;
    }
    @Override
    public void fromMap(Map<String,Object> properties){
        this.index = (String) properties.get("1");
        this.configurationName = (String) properties.get("2");
        this.configurationType = (String) properties.get("3");
        this.configurationTypeId = (String) properties.get("4");
        this.configurationCategory = (String) properties.get("5");
        this.disabled = (boolean)properties.get("6");
    }

    @Override
    public JsonObject toJson(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Name",configurationName);
        jsonObject.addProperty("TypeId",configurationTypeId);
        jsonObject.addProperty("Type",configurationType);
        jsonObject.addProperty("Category",configurationCategory);
        jsonObject.addProperty("RewardKey",this.distributionKey());
        jsonObject.addProperty("Rewarded",disabled);
        return jsonObject;
    }

    public PendingReward toApplication(){
        PendingReward pendingReward = new PendingReward(this);
        pendingReward.distributionKey(this.index);
        return pendingReward;
    }
}
